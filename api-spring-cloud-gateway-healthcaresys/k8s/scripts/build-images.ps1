# Build das 4 imagens Docker a partir da pasta Healthcare
$ErrorActionPreference = "Stop"
$root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent

Write-Host "Build gateway..."
docker build -t healthcare-gateway:latest "$root\api-spring-cloud-gateway-healthcaresys"

Write-Host "Build pacientes..."
docker build -t healthcare-api-pacientes:latest "$root\healthcare-api-pacientes-spring-rabbitmq"

Write-Host "Build usuarios..."
docker build -t healthcare-api-usuarios:latest "$root\healthcare-api-users-microservice-spring-rabbitmq"

Write-Host "Build triagem..."
docker build -t healthcare-api-triagem:latest "$root\healthcare-api-triagem-spring-rabbitmq"

Write-Host "Imagens prontas."
