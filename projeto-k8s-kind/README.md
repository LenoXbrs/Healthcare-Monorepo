# CI/CD com kind (100% gratuito)

## Como funciona

Cada push na `main` dispara o GitHub Actions que:
1. Faz build e push das 4 imagens no GHCR (gratuito)
2. Sobe um cluster `staging` com kind no runner
3. Faz deploy das 4 APIs no staging e valida
4. Aguarda aprovação (environment: production)
5. Sobe um cluster `producao` com kind no runner
6. Faz deploy na produção e valida

Os clusters são efêmeros — sobem e descem a cada pipeline.

## Setup (só uma vez)

### 1. Tornar o pacote GHCR público
Vá em: GitHub → Settings → Packages → Visibility → Public

### 2. Configurar aprovação para produção
Vá em: Repositório → Settings → Environments → New environment
- Nome: `production`
- Marque: "Required reviewers" → adicione seu usuário

### 3. Fazer push e ver a mágica
```bash
git add .
git commit -m "feat: deploy inicial"
git push origin main
```

## Estrutura
```
.github/workflows/ci-cd.yml   ← pipeline completo
api-usuarios/
  Dockerfile
  k8s/
    deployment.yaml
    service.yaml
api-produtos/                  ← mesma estrutura
api-pedidos/                   ← mesma estrutura
api-pagamentos/                ← mesma estrutura
namespaces.yaml
```
