# Deploy no Kubernetes local (minikube ou Docker Desktop K8s)
$ErrorActionPreference = "Stop"
$k8sRoot = Split-Path $PSScriptRoot -Parent

Write-Host "Aplicando manifests (overlay local)..."
kubectl apply -k "$k8sRoot\overlays\local"

Write-Host ""
Write-Host "Aguardando pods..."
kubectl wait --for=condition=ready pod -l app=gateway -n healthcare --timeout=300s 2>$null

Write-Host ""
Write-Host "=== Status ==="
kubectl get pods -n healthcare
Write-Host ""
Write-Host "API (NodePort): http://localhost:30080"
Write-Host "Ou: minikube service gateway-service -n healthcare --url"
Write-Host "Ingress (se nginx instalado): http://healthcare.local (adicione ao hosts)"
