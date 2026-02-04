# kubeadm Installation and Cluster Initialization Guide

## Prerequisites

- Ubuntu 24.04 (or compatible Linux distribution)
- Docker installed and running
- Minimum 2GB RAM
- Minimum 2 CPU cores

## 1. Pre-installation

### Disable Swap (Required)

```bash
# Check swap
sudo swapon --show

# Disable swap (temporary — takes effect immediately for current session)
sudo swapoff -a

# To disable permanently, comment out the swap line in /etc/fstab
# Method 1: Using sed (handles tab characters)
sudo sed -i 's|^/swap.img|#/swap.img|' /etc/fstab

# Method 2: Manual edit with vi
# sudo vi /etc/fstab
# Add # in front of the /swap.img line

# Verify
cat /etc/fstab | grep -i swap

# Swap remains disabled after reboot
```

### Network Configuration (Required)

```bash
# Enable bridge network filtering
cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF

sudo modprobe overlay
sudo modprobe br_netfilter

# Apply network settings
cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF

sudo sysctl --system
```

### Docker Configuration

```bash
# Verify Docker is running
sudo systemctl status docker

# Create/update Docker daemon.json
sudo mkdir -p /etc/docker
cat <<EOF | sudo tee /etc/docker/daemon.json
{
  "exec-opts": ["native.cgroupdriver=systemd"],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m"
  },
  "storage-driver": "overlay2"
}
EOF

sudo systemctl restart docker
```

### Install cri-dockerd (When Using Docker as CRI)

Kubernetes 1.24+ does not support Docker directly. Use cri-dockerd when running Kubernetes with Docker.

```bash
# Install cri-dockerd (using script)
cd <project-dir>/scripts
chmod +x install-cri-dockerd.sh
sudo ./install-cri-dockerd.sh

# Or manual installation
# Check latest release: https://github.com/Mirantis/cri-dockerd/releases
wget https://github.com/Mirantis/cri-dockerd/releases/download/v0.3.9/cri-dockerd_0.3.9.3-0.ubuntu-$(lsb_release -cs)_amd64.deb
sudo dpkg -i cri-dockerd_*.deb
sudo apt-get install -f -y  # Resolve dependencies
sudo systemctl daemon-reload
sudo systemctl enable cri-docker.service
sudo systemctl enable cri-docker.socket
sudo systemctl start cri-docker.socket
sudo systemctl start cri-docker.service
```

## 2. Install kubeadm, kubelet, kubectl

### Automated Installation (Recommended)

```bash
cd <project-dir>/scripts
chmod +x install-kubeadm.sh
sudo ./install-kubeadm.sh
```

### Manual Installation

```bash 
# 1. Install required packages
sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates curl gpg conntrack

# 2. Add Kubernetes GPG key
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.31/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

# 3. Add Kubernetes APT repository
echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.31/deb/ /' | sudo tee /etc/apt/sources.list.d/kubernetes.list

# 4. Install packages
sudo apt-get update
sudo apt-get install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl
```

## 3. Cluster Initialization

### Single-Node Cluster (Test)

**With Docker + cri-dockerd:**
```bash
# Specify Pod network CIDR and cri-dockerd socket
sudo kubeadm init --pod-network-cidr=10.244.0.0/16 --cri-socket=unix:///var/run/cri-dockerd.sock
```

**With containerd:**
```bash
# Specify Pod network CIDR (if needed)
sudo kubeadm init --pod-network-cidr=10.244.0.0/16

# Or use defaults
sudo kubeadm init
```

### Post-Initialization Setup

After initialization, run the following commands (as shown in the output):

```bash
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

### Allow Pods on Control Plane (Single-Node Test)

```bash
kubectl taint nodes --all node-role.kubernetes.io/control-plane-
```

## 4. Install CNI Plugin (Networking)

### Install Flannel (Recommended — Simple)

```bash
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml
```

### Or Install Calico

```bash
kubectl create -f https://raw.githubusercontent.com/projectcalico/calico/v3.27.0/manifests/tigera-operator.yaml
kubectl create -f https://raw.githubusercontent.com/projectcalico/calico/v3.27.0/manifests/custom-resources.yaml
```

## 5. Install Helm

Helm is the Kubernetes package manager and simplifies deployment of applications such as the Hadoop cluster.

### Install Helm

```bash
# Install Helm 3 (latest)
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Or install via apt
curl https://baltocdn.com/helm/signing.asc | gpg --dearmor | sudo tee /usr/share/keyrings/helm.gpg > /dev/null
sudo apt-get install apt-transport-https --yes
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/helm.gpg] https://baltocdn.com/helm/stable/debian/ all main" | sudo tee /etc/apt/sources.list.d/helm-stable-debian.list
sudo apt-get update
sudo apt-get install helm

# Verify installation
helm version
```

### Add Helm Chart Repositories (Optional)

```bash
# Bitnami Helm Charts (includes Hadoop and other applications)
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# List repositories
helm repo list
```

### Helm Usage Examples

```bash
# Search charts
helm search repo hadoop

# Install chart
# helm install <release-name> <chart-name> -n <namespace>

# List releases
helm list -A

# Upgrade chart
# helm upgrade <release-name> <chart-name>

# Uninstall chart
# helm uninstall <release-name>
```

## 6. Install and Configure ArgoCD

ArgoCD is a Kubernetes-native continuous deployment tool for GitOps. It synchronizes and manages Kubernetes manifests from a Git repository.

### Install ArgoCD

```bash
# Create ArgoCD namespace
kubectl create namespace argocd

# Install ArgoCD (official manifest)
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Or install with Helm (recommended)
helm repo add argo https://argoproj.github.io/argo-helm
helm repo update
helm install argocd argo/argo-cd -n argocd --create-namespace

# Wait for ArgoCD pods to reach Running
kubectl get pods -n argocd -w
```

### Configure ArgoCD Access

```bash
# Change to NodePort for external access
kubectl patch svc argocd-server -n argocd -p '{"spec": {"type": "NodePort"}}'

# Or use port forwarding
kubectl port-forward svc/argocd-server -n argocd 8080:443

# Get initial admin password
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d && echo

# Install ArgoCD CLI (optional)
curl -sSL -o /usr/local/bin/argocd https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-amd64
chmod +x /usr/local/bin/argocd

# ArgoCD login (when using CLI)
# argocd login localhost:8080
```

### ArgoCD Usage Examples

```bash
# Create Application (command line)
kubectl create -f - <<EOF
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: hadoop-cluster
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/your-org/hadoop-k8s-manifests.git
    path: manifests/
    targetRevision: HEAD
  destination:
    server: https://kubernetes.default.svc
    namespace: hadoop
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
    - CreateNamespace=true
EOF

# Or create from YAML file
# kubectl apply -f manifests/argocd/hadoop-application.yaml

# Check Application status
kubectl get applications -n argocd

# Web UI access
# Browser: https://localhost:8080 (when using port forwarding)
# Or with NodePort: https://<node-ip>:<nodeport>
```

### ArgoCD with Helm

ArgoCD supports Helm charts:

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: hadoop-helm
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://charts.bitnami.com/bitnami
    chart: hadoop
    targetRevision: latest
    helm:
      values: |
        nameNode:
          replicas: 1
        dataNode:
          replicas: 3
  destination:
    server: https://kubernetes.default.svc
    namespace: hadoop
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
```

## 7. Verify Cluster Status

```bash
# Check node status
kubectl get nodes

# Check all pods
kubectl get pods --all-namespaces

# Cluster info
kubectl cluster-info
```

## 8. Troubleshooting

### cri-dockerd Errors

**Error: "failed to create new CRI runtime service"**

cri-dockerd is required when using Docker:

```bash
# Check cri-dockerd status
sudo systemctl status cri-docker.socket
sudo systemctl status cri-docker.service

# Restart cri-dockerd
sudo systemctl restart cri-docker.socket
sudo systemctl restart cri-docker.service

# Verify cri-socket option when running kubeadm init
sudo kubeadm init --pod-network-cidr=10.244.0.0/16 --cri-socket=unix:///var/run/cri-dockerd.sock
```

### kubelet Fails to Start

```bash
# Check kubelet status
sudo systemctl status kubelet

# View logs
sudo journalctl -xeu kubelet
```

### Image Pull Errors

```bash
# Pre-pull images
sudo kubeadm config images pull
```

### Cluster Reset

```bash
# Reset cluster
sudo kubeadm reset
sudo rm -rf /etc/cni/net.d
sudo rm -rf /var/lib/etcd
```

### CoreDNS 0/1 Ready, Readiness 503 (Pod → API Unreachable)

**Symptoms:** CoreDNS shows `0/1 Running` in `kubectl get pods -A`, with event `Readiness probe failed: HTTP 503`.  
CoreDNS logs show `Get "https://10.96.0.1:443/...": dial tcp 10.96.0.1:443: i/o timeout`.

**Cause:** Pods cannot reach the Kubernetes API (ClusterIP `10.96.0.1` or node `:6443`).  
`curl -sk https://10.96.0.1:443/healthz` works from the host but fails from inside a pod.

**Diagnosis:**

```bash
# 1) Test Pod → ClusterIP
kubectl run debug-api --restart=Never --image=curlimages/curl -- \
  sh -c 'curl -sk --connect-timeout 5 https://10.96.0.1:443/healthz || echo FAIL'
kubectl logs debug-api

# 2) Test Pod → node IP:6443 (endpoint)
kubectl get endpoints kubernetes -o jsonpath='{.subsets[0].addresses[0].ip}'
kubectl run debug-api2 --restart=Never --image=curlimages/curl -- \
  sh -c 'curl -sk --connect-timeout 5 https://<ENDPOINT_IP>:6443/healthz || echo FAIL'
kubectl logs debug-api2

kubectl delete pod debug-api debug-api2 --ignore-not-found
```

**Fix (in order of priority):**

1. **Firewall**  
   Verify that traffic is allowed between Pod CIDR (e.g. `10.244.0.0/16`) and node IP, and Service CIDR `10.96.0.0/12`.

   ```bash
   sudo ufw status   # or firewalld
   # Allow Pod/Service CIDRs if needed, then reload
   ```

2. **CoreDNS hostNetwork Workaround (Single-Node)**  
   Use host network for CoreDNS to bypass pod network issues.

   ```bash
   kubectl -n kube-system edit deployment coredns
   # Add under spec.template.spec:
   #   hostNetwork: true
   #   dnsPolicy: ClusterFirstWithHostNet
   ```

3. **Flannel / CNI**  
   Check `kubectl get pods -n kube-flannel`.  
   Search [Flannel issues](https://github.com/flannel-io/flannel/issues) for single-node or pod→node connectivity problems.

4. **iptables / kube-proxy**  
   Inspect `iptables -L -n -v` and `iptables -t nat -L -n -v` for `KUBE-SVC-*` and `10.96.0.1` rules.

## 9. References

- [kubeadm Official Documentation](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/)
- [Kubernetes Installation Guide](https://kubernetes.io/docs/setup/)
- [Helm Official Documentation](https://helm.sh/docs/)
- [Bitnami Helm Charts](https://charts.bitnami.com/)
