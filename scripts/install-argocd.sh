#!/bin/bash
# ArgoCD 설치 스크립트

set -e

echo "🚀 ArgoCD 설치를 시작합니다..."

# kubectl이 설치되어 있는지 확인
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl이 설치되어 있지 않습니다."
    echo "   먼저 Kubernetes 클러스터를 초기화하세요."
    exit 1
fi

# Kubernetes 클러스터 연결 확인
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ Kubernetes 클러스터에 연결할 수 없습니다."
    echo "   먼저 클러스터를 초기화하고 kubectl을 설정하세요."
    exit 1
fi

INSTALL_METHOD=${1:-kubectl}

if [ "$INSTALL_METHOD" = "helm" ]; then
    echo "📦 Helm을 통한 ArgoCD 설치..."
    
    # Helm이 설치되어 있는지 확인
    if ! command -v helm &> /dev/null; then
        echo "❌ Helm이 설치되어 있지 않습니다."
        echo "   먼저 Helm을 설치하세요: <project-dir>/scripts/install-helm.sh"
        exit 1
    fi
    
    # ArgoCD Helm 저장소 추가
    helm repo add argo https://argoproj.github.io/argo-helm
    helm repo update
    
    # ArgoCD 설치
    kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f -
    helm install argocd argo/argo-cd -n argocd --create-namespace
    
    echo "⏳ ArgoCD Pod들이 준비될 때까지 대기 중..."
    kubectl wait --for=condition=ready pod --all -n argocd --timeout=300s
    
else
    echo "📦 kubectl을 통한 ArgoCD 설치 (공식 매니페스트)..."
    
    # ArgoCD 네임스페이스 생성
    kubectl create namespace argocd
    
    # ArgoCD 설치
    kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
    
    echo "⏳ ArgoCD Pod들이 준비될 때까지 대기 중..."
    kubectl wait --for=condition=ready pod --all -n argocd --timeout=300s
fi

echo ""
echo "✅ ArgoCD 설치 완료!"
echo ""

# ArgoCD 서비스 상태 확인
echo "📋 ArgoCD 서비스 상태:"
kubectl get svc -n argocd

echo ""
echo "📝 다음 단계:"
echo ""
echo "1. ArgoCD 서버 접근 설정:"
echo "   # 포트 포워딩 (로컬 접근)"
echo "   kubectl port-forward svc/argocd-server -n argocd 8080:443"
echo ""
echo "   # 또는 NodePort로 변경 (외부 접근)"
echo "   kubectl patch svc argocd-server -n argocd -p '{\"spec\": {\"type\": \"NodePort\"}}'"
echo ""
echo "2. 초기 admin 비밀번호 확인:"
echo "   kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath=\"{.data.password}\" | base64 -d && echo"
echo ""
echo "3. Web UI 접근:"
echo "   브라우저에서 https://localhost:8080 접속 (포트 포워딩 사용시)"
echo ""
echo "4. ArgoCD CLI 설치 (선택사항):"
echo "   curl -sSL -o /usr/local/bin/argocd https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-amd64"
echo "   chmod +x /usr/local/bin/argocd"
echo ""
echo "📚 상세 가이드: <project-dir>/docs/kubeadm-setup.md"
