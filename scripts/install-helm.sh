#!/bin/bash
# Helm 설치 스크립트 (Ubuntu 24.04)

set -e

echo "🚀 Helm 설치를 시작합니다..."

# Helm 3 설치 방법 선택
INSTALL_METHOD=${1:-curl}

if [ "$INSTALL_METHOD" = "apt" ]; then
    echo "📦 APT를 통한 Helm 설치..."
    curl https://baltocdn.com/helm/signing.asc | gpg --dearmor | sudo tee /usr/share/keyrings/helm.gpg > /dev/null
    sudo apt-get install apt-transport-https --yes
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/helm.gpg] https://baltocdn.com/helm/stable/debian/ all main" | sudo tee /etc/apt/sources.list.d/helm-stable-debian.list
    sudo apt-get update
    sudo apt-get install -y helm
else
    echo "📦 공식 설치 스크립트를 통한 Helm 설치..."
    curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
fi

# 설치 확인
echo ""
echo "✅ Helm 설치 완료!"
echo ""
echo "📋 설치된 버전:"
helm version

# Bitnami 저장소 추가 (선택사항)
echo ""
read -p "Bitnami Helm Charts 저장소를 추가하시겠습니까? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "📚 Bitnami Helm Charts 저장소 추가 중..."
    helm repo add bitnami https://charts.bitnami.com/bitnami
    helm repo update
    echo "✅ Bitnami 저장소가 추가되었습니다."
fi

echo ""
echo "📝 사용 예시:"
echo "   helm repo list                    # 저장소 목록 확인"
echo "   helm search repo hadoop           # Hadoop Chart 검색"
echo "   helm install <name> <chart>       # Chart 설치"
