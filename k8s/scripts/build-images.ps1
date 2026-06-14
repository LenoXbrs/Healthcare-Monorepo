# Build das 4 imagens Docker a partir da pasta Healthcare
$ErrorActionPreference = "Stop"
$root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent

Write-Host "Compilando todos os projetos localmente com Maven..."
& "$root\healthcare-api-triagem-spring-rabbitmq\mvnw.cmd" -f "$root\api-spring-cloud-gateway-healthcaresys\pom.xml" clean package -DskipTests -q
& "$root\healthcare-api-triagem-spring-rabbitmq\mvnw.cmd" -f "$root\healthcare-api-pacientes-spring-rabbitmq\pom.xml" clean package -DskipTests -q
& "$root\healthcare-api-triagem-spring-rabbitmq\mvnw.cmd" -f "$root\healthcare-api-users-microservice-spring-rabbitmq\pom.xml" clean package -DskipTests -q
& "$root\healthcare-api-triagem-spring-rabbitmq\mvnw.cmd" -f "$root\healthcare-api-triagem-spring-rabbitmq\pom.xml" clean package -DskipTests -q

Write-Host "Build gateway..."
docker build --no-cache -t healthcare-gateway:latest "$root\api-spring-cloud-gateway-healthcaresys"

Write-Host "Build pacientes..."
docker build --no-cache -t healthcare-api-pacientes:latest "$root\healthcare-api-pacientes-spring-rabbitmq"

Write-Host "Build usuarios..."
docker build --no-cache -t healthcare-api-usuarios:latest "$root\healthcare-api-users-microservice-spring-rabbitmq"

Write-Host "Build triagem..."
docker build --no-cache -t healthcare-api-triagem:latest "$root\healthcare-api-triagem-spring-rabbitmq"

Write-Host "Imagens prontas."
