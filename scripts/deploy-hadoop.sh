#!/bin/bash
set -e
# Hadoop 클러스터 배포 (순서 중요)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${ROOT_DIR}"

# StorageClass 없으면 local-path-provisioner 설치
if ! kubectl get storageclass local-path &>/dev/null; then
  echo "=== 0. Local Path Provisioner (StorageClass) ==="
  kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/master/deploy/local-path-storage.yaml
  echo "   Waiting for provisioner ready..."
  sleep 10
fi

echo "=== 1. Namespace ==="
kubectl apply -f manifests/namespace/

echo "=== 2. ConfigMap ==="
kubectl apply -f manifests/configmap/

echo "=== 3. Services ==="
kubectl apply -f manifests/service/services.yaml

echo "=== 4. NameNode (StatefulSet) ==="
kubectl apply -f manifests/statefulset/namenode.yaml

echo "=== 5. DataNode (StatefulSet) - NameNode 기다림 ==="
kubectl rollout status statefulset/namenode -n hadoop --timeout=120s
# volumeClaimTemplates→emptyDir 변경 시 삭제 후 재생성 필요
kubectl delete statefulset datanode -n hadoop --ignore-not-found 2>/dev/null || true
sleep 3
kubectl apply -f manifests/statefulset/datanode.yaml

echo "=== 6. ResourceManager, NodeManager, HistoryServer ==="
kubectl apply -f manifests/statefulset/resourcemanager.yaml
kubectl apply -f manifests/statefulset/nodemanager.yaml
kubectl apply -f manifests/statefulset/historyserver.yaml

echo "=== 7. NodePort (UI 접근용, 옵션) ==="
kubectl apply -f manifests/service/nodeport.yaml

echo ""
echo "배포 완료. Pod 기동 대기..."
kubectl rollout status statefulset/namenode -n hadoop --timeout=180s
kubectl rollout status statefulset/datanode -n hadoop --timeout=180s

echo ""
echo "=== UI 접속 (NodePort) ==="
echo "  NameNode:      http://<node-ip>:30070"
echo "  ResourceManager: http://<node-ip>:30088"
echo "  HistoryServer:   http://<node-ip>:30188"
echo ""
echo "  kubectl get pods -n hadoop"
echo "  kubectl logs -n hadoop -l app=namenode -f"
echo ""
