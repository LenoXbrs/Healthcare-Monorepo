# Script para compilação local e geração das imagens Docker para todas as APIs e Frontend do Healthcare.
# Uso: .\build-and-push-images.ps1 -GithubUsername "seu-usuario-github" -Push $true

param (
    [string]$GithubUsername = "",
    [bool]$Push = $false,
    [string]$Registry = "ghcr.io"
)

# Se for empurrar para o registry, precisa do username do GitHub/Registry
if ($Push -and [string]::IsNullOrEmpty($GithubUsername)) {
    Write-Error "Para fazer push das imagens, voce precisa fornecer o parametro -GithubUsername 'seu-usuario'."
    exit 1
}

$ErrorActionPreference = "Stop"

# 1. Compilar Projetos Backend (Java/Spring)
$javaProjects = @(
    "api-spring-cloud-gateway-healthcaresys",
    "healthcare-api-pacientes-spring-rabbitmq",
    "healthcare-api-users-microservice-spring-rabbitmq",
    "healthcare-api-triagem-spring-rabbitmq"
)

Write-Host "==============================================" -ForegroundColor Green
Write-Host "1. Compilando os microserviços Java/Spring..." -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Green

foreach ($project in $javaProjects) {
    Write-Host "-> Compilando $project..." -ForegroundColor Cyan
    Push-Location $project
    try {
        # Executa o wrapper do maven
        & .\mvnw.cmd clean package -DskipTests
    } finally {
        Pop-Location
    }
}

# 2. Compilar Frontend (Angular)
Write-Host "==============================================" -ForegroundColor Green
Write-Host "2. Compilando o Frontend Angular..." -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Green

Push-Location "healthcare-front-end"
try {
    Write-Host "-> Instalando dependencias e rodando build..." -ForegroundColor Cyan
    npm install
    npm run build
} finally {
    Pop-Location
}

# 3. Construir as imagens Docker
Write-Host "==============================================" -ForegroundColor Green
Write-Host "3. Construindo as imagens Docker..." -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Green

$images = @{
    "healthcare-gateway"      = "api-spring-cloud-gateway-healthcaresys"
    "healthcare-api-pacientes" = "healthcare-api-pacientes-spring-rabbitmq"
    "healthcare-api-usuarios"  = "healthcare-api-users-microservice-spring-rabbitmq"
    "healthcare-api-triagem"   = "healthcare-api-triagem-spring-rabbitmq"
    "healthcare-front-end"     = "healthcare-front-end"
}

foreach ($imgName in $images.Keys) {
    $dir = $images[$imgName]
    $tagLocal = "$imgName:latest"
    Write-Host "-> docker build -t $tagLocal em $dir" -ForegroundColor Cyan
    docker build -t $tagLocal $dir

    if ($Push) {
        $tagRegistry = "$Registry/$($GithubUsername.ToLower())/$imgName:latest"
        Write-Host "-> docker tag $tagLocal $tagRegistry" -ForegroundColor Cyan
        docker tag $tagLocal $tagRegistry
        
        Write-Host "-> docker push $tagRegistry" -ForegroundColor Cyan
        docker push $tagRegistry
    }
}

Write-Host "==============================================" -ForegroundColor Green
Write-Host "Processo concluído com sucesso!" -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Green
