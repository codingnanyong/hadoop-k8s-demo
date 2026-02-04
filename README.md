# ☸️ Hadoop on Kubernetes

HDFS + YARN test cluster on Kubernetes (kubeadm).

[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.x-326CE5?logo=kubernetes&logoColor=white)](https://kubernetes.io/)
[![Hadoop](https://img.shields.io/badge/Apache%20Hadoop-3.4-CC0000?logo=apache&logoColor=white)](https://hadoop.apache.org/)
[![HDFS](https://img.shields.io/badge/HDFS-Distributed%20Storage-66CCFF)](https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html)
[![YARN](https://img.shields.io/badge/YARN-Resource%20Manager-66CCFF)](https://hadoop.apache.org/docs/stable/hadoop-yarn/hadoop-yarn-site/YARN.html)
[![kubeadm](https://img.shields.io/badge/kubeadm-Cluster%20Setup-326CE5?logo=kubernetes&logoColor=white)](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/)
[![Docker](https://img.shields.io/badge/Docker-Images-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)

## 📁 Project Structure

```text
hadoop-k8s/
├── manifests/
│   ├── namespace/
│   ├── configmap/
│   ├── statefulset/
│   ├── service/
│   └── pvc/
├── config/
├── scripts/
│   ├── deploy-hadoop.sh
│   ├── install-kubeadm.sh
│   └── install-helm.sh
├── docs/
    ├── Architecture.md
    └── kubeadm-setup.md
```

## 📋 Prerequisites

- Kubernetes cluster (kubeadm, single or multi-node)
- CNI plugin (e.g., Flannel)
- Optional: Helm

See [docs/kubeadm-setup.md](docs/kubeadm-setup.md) for cluster setup.

---

## ⚙️ Installation & Deployment

### 1. 🔧 Install Kubernetes (if not yet installed)

```bash
# <project-dir> = path where hadoop-k8s is cloned (e.g. ~/hadoop-k8s)
cd <project-dir>/scripts
chmod +x install-kubeadm.sh
sudo ./install-kubeadm.sh
```

### 2. 🗂️ Deployment Order

| Step | Action | Command / Resource |
| ------ | ------ | ------ |
| 0 | Install StorageClass (if missing) | local-path-provisioner (auto by deploy script) |
| 1 | Create namespace | `manifests/namespace/` |
| 2 | Create ConfigMaps | `manifests/configmap/` |
| 3 | Create Services | `manifests/service/services.yaml` |
| 4 | Deploy NameNode | `manifests/statefulset/namenode.yaml` |
| 5 | Deploy DataNode | `manifests/statefulset/datanode.yaml` |
| 6 | Deploy ResourceManager, NodeManager, HistoryServer | `manifests/statefulset/*.yaml` |
| 7 | (Optional) NodePort for Web UIs | `manifests/service/nodeport.yaml` |

### 3. 🚀 One-command Deployment

```bash
cd <project-dir>  # e.g. cd ~/hadoop-k8s
./scripts/deploy-hadoop.sh
```

### 4. 📝 Manual Deployment

```bash
kubectl apply -f manifests/namespace/
kubectl apply -f manifests/configmap/
kubectl apply -f manifests/service/services.yaml
kubectl apply -f manifests/statefulset/namenode.yaml
kubectl rollout status statefulset/namenode -n hadoop --timeout=120s
kubectl apply -f manifests/statefulset/datanode.yaml
kubectl apply -f manifests/statefulset/resourcemanager.yaml
kubectl apply -f manifests/statefulset/nodemanager.yaml
kubectl apply -f manifests/statefulset/historyserver.yaml
kubectl apply -f manifests/service/nodeport.yaml
```

---

## 🌐 Web UIs (NodePort)

| Component | URL |
| ------ | ------ |
| NameNode | http://\<node-ip\>:30070 |
| ResourceManager | http://\<node-ip\>:30088 |
| HistoryServer | http://\<node-ip\>:30188 |

---

## 🧪 HDFS Test

```bash
kubectl run hdfs-client -n hadoop --rm -it --restart=Never --image=apache/hadoop:3.4.0 -- \
  hdfs dfs -ls hdfs://namenode-svc:8020/
```

---

## 🏗️ Architecture & Storage

See [docs/Architecture.md](docs/Architecture.md) for:

- Component layout
- **Storage architecture** (where data is stored)
- NameNode: Persistent (PVC via local-path)
- DataNode: Ephemeral (emptyDir) — test only

---

## 📝 Notes

- This setup is for **test environments**.
- DataNode data is **not persistent**; use PVC for production.
- Production use requires security hardening and tuning.

---

**Last Updated**: February 2026
