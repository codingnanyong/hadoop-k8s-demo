#!/bin/bash
# cri-dockerd 설치 스크립트 (Ubuntu 24.04)
# Docker를 Kubernetes CRI로 사용하기 위한 어댑터

set -e

echo "🚀 cri-dockerd 설치를 시작합니다..."

# 최신 버전 확인 (GitHub Releases에서)
CRI_DOCKERD_VERSION=${1:-"0.3.21"}
ARCH=$(dpkg --print-architecture)
UBUNTU_CODENAME=$(lsb_release -cs)

# AMD64 또는 ARM64 확인
if [ "$ARCH" = "amd64" ]; then
    ARCH="amd64"
elif [ "$ARCH" = "arm64" ]; then
    ARCH="arm64"
else
    echo "❌ 지원하지 않는 아키텍처: $ARCH"
    exit 1
fi

echo "📦 cri-dockerd ${CRI_DOCKERD_VERSION} (${ARCH}) 다운로드 중..."

# 임시 디렉토리 생성
TMP_DIR=$(mktemp -d)
cd "$TMP_DIR"

# Ubuntu 24.04 (noble)는 Debian bookworm 패키지 사용 가능
# 여러 배포판명을 시도
DISTRO_NAMES=("ubuntu-${UBUNTU_CODENAME}" "debian-bookworm" "debian-bullseye" "ubuntu-jammy" "ubuntu-focal")
DOWNLOAD_URL=""
DOWNLOAD_FILE=""

for DISTRO in "${DISTRO_NAMES[@]}"; do
    URL="https://github.com/Mirantis/cri-dockerd/releases/download/v${CRI_DOCKERD_VERSION}/cri-dockerd_${CRI_DOCKERD_VERSION}.3-0.${DISTRO}_${ARCH}.deb"
    if wget -q --spider "$URL" 2>/dev/null; then
        DOWNLOAD_URL="$URL"
        DOWNLOAD_FILE="cri-dockerd_${CRI_DOCKERD_VERSION}.3-0.${DISTRO}_${ARCH}.deb"
        echo "✅ 패키지 발견: ${DISTRO}"
        break
    fi
done

if [ -z "$DOWNLOAD_URL" ]; then
    echo "❌ cri-dockerd 패키지를 찾을 수 없습니다."
    echo "   수동 설치 방법:"
    echo "   1. https://github.com/Mirantis/cri-dockerd/releases 에서 적절한 .deb 파일 다운로드"
    echo "   2. sudo dpkg -i cri-dockerd_*.deb"
    echo "   3. sudo apt-get install -f -y"
    rm -rf "$TMP_DIR"
    exit 1
fi

# cri-dockerd 다운로드
wget -q "$DOWNLOAD_URL" -O "$DOWNLOAD_FILE" || {
    echo "❌ cri-dockerd 다운로드 실패"
    echo "   URL: $DOWNLOAD_URL"
    echo "   최신 버전을 확인하세요: https://github.com/Mirantis/cri-dockerd/releases"
    rm -rf "$TMP_DIR"
    exit 1
}

# 설치
echo "⬇️ cri-dockerd 설치 중..."
sudo dpkg -i "$DOWNLOAD_FILE" || {
    echo "⚠️ 의존성 문제 발생, 자동으로 해결 중..."
    sudo apt-get install -f -y
    sudo dpkg -i "$DOWNLOAD_FILE"
}

# 시스템 서비스 활성화 및 시작
echo "🔧 cri-dockerd 서비스 설정 중..."
sudo systemctl daemon-reload
sudo systemctl enable cri-docker.service
sudo systemctl enable cri-docker.socket
sudo systemctl start cri-docker.socket
sudo systemctl start cri-docker.service

# 설치 확인
echo ""
echo "✅ cri-dockerd 설치 완료!"
echo ""
echo "📋 서비스 상태:"
sudo systemctl status cri-docker.socket --no-pager -l || true

echo ""
echo "📝 다음 단계:"
echo "1. kubeadm init 실행 시 --cri-socket 옵션 사용:"
echo "   sudo kubeadm init --pod-network-cidr=10.244.0.0/16 --cri-socket=unix:///var/run/cri-dockerd.sock"
echo ""
echo "📚 상세 가이드: <project-dir>/docs/kubeadm-setup.md"

# 임시 디렉토리 정리
cd -
rm -rf "$TMP_DIR"
