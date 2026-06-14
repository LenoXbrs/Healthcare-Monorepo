# Deploy Kubernetes — Healthcare

Stack completa no namespace `healthcare`:

| Componente | Tipo K8s | Porta |
|------------|----------|-------|
| PostgreSQL (3 DBs) | StatefulSet | 5432 |
| Redis | Deployment | 6379 |
| RabbitMQ | Deployment | 5672 / 15672 |
| api-pacientes | Deployment + Service | 8081 |
| api-usuarios | Deployment + Service | 8082 |
| api-triagem | Deployment + Service | 8083 |
| api-gateway | Deployment + Service (NodePort 30080) | 8080 |

## Pré-requisitos

- Docker
- `kubectl`
- Cluster local: **Docker Desktop (Kubernetes)** ou **minikube** ou **kind**
- (Opcional) Ingress NGINX: `kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.11.3/deploy/static/provider/cloud/deploy.yaml`

## Passo a passo (entrega / demonstração)

### 1. Build das imagens

```powershell
cd k8s\scripts
.\build-images.ps1
```

**kind:** depois rode `.\kind-load-images.ps1`

**minikube:** `minikube image load healthcare-gateway:latest` (repita para as 4 imagens)

### 2. Deploy

```powershell
.\deploy-local.ps1
```

Ou manualmente:

```bash
kubectl apply -k k8s/overlays/local
kubectl get pods -n healthcare -w
```

### 3. Testar API

- NodePort: `http://localhost:30080/pacientes` (com JWT conforme sua API)
- Swagger gateway: `http://localhost:30080` (rotas dos microsserviços via gateway)

### 4. RabbitMQ Management (opcional)

```bash
kubectl port-forward -n healthcare svc/rabbitmq-service 15672:15672
```

Abra `http://localhost:15672` — usuário `guest`, senha `guest`.

## CI/CD (GitHub Actions + cluster)

1. Build e push das 4 imagens para GHCR (workflow em cada repositório).
2. Edite `overlays/ghcr/kustomization.yaml` — troque `SEU_USUARIO`.
3. Configure secret `KUBECONFIG` no GitHub (base64 do kubeconfig).
4. Workflow `deploy-kubernetes.yml` no repositório do gateway executa `kubectl apply -k k8s/overlays/ghcr`.

## Estrutura

```
k8s/
  base/           # Manifests principais
  overlays/
    local/        # Imagens locais (desenvolvimento)
    ghcr/         # Imagens no GitHub Container Registry
  scripts/        # build-images.ps1, deploy-local.ps1
```

## Limpar cluster

```bash
kubectl delete namespace healthcare
```
