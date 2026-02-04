# Hadoop on Kubernetes — Architecture

## Overview

This document describes the storage architecture and component layout of the Hadoop cluster deployed on Kubernetes.

---

## 1. Component Architecture

```text
                    ┌─────────────────────────────────────────────────────────────┐
                    │                    Kubernetes Cluster (node)                 │
                    │                                                             │
  ┌─────────────────┴──────────────────────────────────────────────────────────┐  │
  │                         hadoop namespace                                   │  │
  │                                                                            │  │
  │  ┌───────────────┐  ┌───────────────┐  ┌─────────────────────┐             │  │
  │  │  NameNode     │  │ResourceManager│  │   HistoryServer     │             │  │
  │  │  (StatefulSet)│  │ (Deployment)  │  │   (Deployment)      │             │  │
  │  │  1 replica    │  │  1 replica    │  │    1 replica        │             │  │
  │  └──────┬────────┘  └───────────────┘  └─────────────────────┘             │  │
  │         │                                                                  │  │
  │         │ HDFS metadata                                                    │  │
  │         │ Block locations                                                  │  │
  │         ▼                                                                  │  │
  │  ┌───────────────┐  ┌───────────────┐                                      │  │
  │  │  DataNode-0   │  │  DataNode-1   │                                      │  │
  │  │  (StatefulSet)│  │  (StatefulSet)│                                      │  │
  │  │  HDFS blocks  │  │  HDFS blocks  │                                      │  │
  │  └───────────────┘  └───────────────┘                                      │  │
  │         ▲                                                                  │  │
  │         │                                                                  │  │
  │  ┌──────┴───────┐  ┌──────────────┐                                        │  │
  │  │ NodeManager  │  │ NodeManager  │   (YARN workers)                       │  │
  │  │ (Deployment) │  │ (Deployment) │                                        │  │
  │  └──────────────┘  └──────────────┘                                        │  │
  │                                                                            │  │
  └────────────────────────────────────────────────────────────────────────────┘  │
                    └─────────────────────────────────────────────────────────────┘
```

---

## 2. Storage Architecture

### 2.1 NameNode Storage (Persistent)

| Attribute | Value |
| ------ | ------ |
| **Purpose** | HDFS metadata (fsimage, edit logs, namespace) |
| **Container Path** | `/hadoop/dfs/name` |
| **Volume Type** | PersistentVolumeClaim (StorageClass: `local-path`) |
| **Capacity** | 2 Gi |
| **Host Path** | `/opt/local-path-provisioner/<pvc-xxx>/` on the node |
| **Persistence** | **Yes** — Data survives pod/container restart |
| **PVC Name** | `namenode-data-namenode-0` |

The local-path-provisioner creates a directory on the node (default: `/opt/local-path-provisioner`) and binds it to the PVC. All HDFS metadata is stored here.

### 2.2 DataNode Storage (Ephemeral)

| Attribute | Value |
| ------ | ------ |
| **Purpose** | HDFS block data (actual file contents) |
| **Container Path** | `/hadoop/dfs/data` |
| **Volume Type** | `emptyDir` |
| **Persistence** | **No** — Data is **lost** when the pod restarts or is rescheduled |
| **Note** | Test environment only; production should use PVC or dedicated storage |

Each DataNode pod has its own `emptyDir` volume. Data is stored in the container’s overlay filesystem. When the pod is deleted or recreated, all block data is lost.

### 2.3 Storage Flow

```
User writes file to HDFS
        │
        ▼
┌───────────────┐     metadata      ┌─────────────────┐
│   NameNode    │◄──────────────────│  Client / RM    │
│ (Persistent)  │                   └─────────────────┘
└───────┬───────┘
        │ Block locations
        ▼
┌───────────────┐     blocks        ┌───────────────┐
│  DataNode-0   │◄──────────────────│  Client       │
│ (Ephemeral)   │                   └───────────────┘
└───────────────┘
        │
┌───────────────┐
│  DataNode-1   │  (replication = 1 in test env)
│ (Ephemeral)   │
└───────────────┘
```

### 2.4 Configuration Storage

| Component | Source | Mount Path |
| ------ | ------ | ------ |
| core-site.xml, hdfs-site.xml, yarn-site.xml, mapred-site.xml, workers | ConfigMap `hadoop-config` | `/opt/hadoop/etc/hadoop` |
| HistoryServer mapred-site | ConfigMap `historyserver-mapred` (merged via init) | `/opt/hadoop/etc/hadoop` |

---

## 3. Network & Services

| Service | Type | Port | Purpose |
| ------ | ------ | ------ | ------ |
| namenode-svc | Headless (ClusterIP: None) | 8020 (RPC), 9870 (HTTP) | NameNode discovery |
| datanode-svc | Headless | 9864, 9866 | DataNode discovery |
| resourcemanager-svc | ClusterIP | 8088 | ResourceManager |
| historyserver-svc | ClusterIP | 19888 | Job history |
| namenode-ui | NodePort | 30070 | Web UI (external) |
| resourcemanager-ui | NodePort | 30088 | Web UI (external) |
| historyserver-ui | NodePort | 30188 | Web UI (external) |

---

## 4. Production Considerations

For production deployments, consider:

1. **DataNode persistence**: Replace `emptyDir` with `volumeClaimTemplates` (PVC) or dedicated storage (e.g., EBS, NFS).
2. **NameNode HA**: Add standby NameNode and shared storage (e.g., NFS, K8s RWX volume).
3. **ResourceManager HA**: Configure HA with ZooKeeper.
4. **StorageClass**: Use a production-grade StorageClass (e.g., cloud provider CSI).
5. **Resource limits**: Tune CPU/memory requests and limits for workloads.

---

## 5. Data Location Summary

| Data Type | Where It Is Stored | Persistent? |
| ------ | ------ | ------ |
| HDFS metadata | Host: `/opt/local-path-provisioner/...` (via local-path PVC) | Yes |
| HDFS block data | Container overlay (emptyDir) | No |
| YARN logs | Container filesystem | No |
| MapReduce job history | HistoryServer container | No |
