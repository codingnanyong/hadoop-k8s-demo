#!/bin/bash
# kubeadm, kubelet, kubectl 설치 스크립트 (Ubuntu 24.04)

set -e

echo "🚀 Kubernetes (kubeadm) 설치를 시작합니다..."

# 1. 필요한 패키지 설치
echo "📦 필수 패키지 설치 중..."
sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates curl gpg conntrack

# 2. Kubernetes 공식 GPG 키 추가
echo "🔑 Kubernetes GPG 키 추가 중..."
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.31/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

# 3. Kubernetes APT 저장소 추가
echo "📚 Kubernetes APT 저장소 추가 중..."
echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.31/deb/ /' | sudo tee /etc/apt/sources.list.d/kubernetes.list

# 4. 패키지 목록 업데이트
echo "🔄 패키지 목록 업데이트 중..."
sudo apt-get update

# 5. kubelet, kubeadm, kubectl 설치
echo "⬇️ kubelet, kubeadm, kubectl 설치 중..."
sudo apt-get install -y kubelet kubeadm kubectl

# 6. 버전 고정 (자동 업그레이드 방지)
echo "📌 패키지 버전 고정 중..."
sudo apt-mark hold kubelet kubeadm kubectl

# 7. 설치 확인
echo ""
echo "✅ 설치 완료!"
echo ""
echo "📋 설치된 버전:"
kubeadm version
kubectl version --client
kubelet --version

echo ""
echo "📝 다음 단계:"
echo "1. cri-dockerd 설치 (Docker 사용 시 필수)"
echo "   <project-dir>/scripts/install-cri-dockerd.sh"
echo "2. kubeadm init 명령으로 클러스터 초기화"
echo "   sudo kubeadm init --pod-network-cidr=10.244.0.0/16 --cri-socket=unix:///var/run/cri-dockerd.sock"
echo "3. kubectl 설정 (kubeadm init 출력 메시지 참고)"
echo "4. 네트워크 플러그인 설치 (CNI)"
echo "5. Helm 설치 (선택사항 - 패키지 관리용)"
echo "6. ArgoCD 설치 (선택사항 - GitOps 배포용)"
echo ""
echo "⚠️ 주의: kubeadm init 실행 전에 다음을 확인하세요:"
echo "   - Docker가 실행 중인지"
echo "   - cri-dockerd가 설치되고 실행 중인지 (Docker 사용 시)"
echo "   - Swap이 비활성화되어 있는지 (sudo swapoff -a)"
echo "   - 호스트 이름이 올바른지"
echo ""
echo "📚 상세 가이드: <project-dir>/docs/kubeadm-setup.md"