#!/bin/bash
# Script para compilação local e geração das imagens Docker para todas as APIs e Frontend do Healthcare.
# Uso: ./build-and-push-images.sh -u seu-usuario-github -p

set -e

GITHUB_USERNAME=""
PUSH=false
REGISTRY="ghcr.io"

while getopts "u:pr:" opt; do
  case $opt in
    u) GITHUB_USERNAME="$OPTARG" ;;
    p) PUSH=true ;;
    r) REGISTRY="$OPTARG" ;;
    *) echo "Uso: $0 [-u usuario-github] [-p] [-r registry]" && exit 1 ;;
  esac
done

if [ "$PUSH" = true ] && [ -z "$GITHUB_USERNAME" ]; then
    echo "Erro: Para fazer push das imagens, você precisa fornecer o usuário do GitHub/Registry com o parâmetro -u."
    exit 1
fi

echo "=============================================="
echo "1. Compilando os microserviços Java/Spring..."
echo "=============================================="

javaProjects=(
    "api-spring-cloud-gateway-healthcaresys"
    "healthcare-api-pacientes-spring-rabbitmq"
    "healthcare-api-users-microservice-spring-rabbitmq"
    "healthcare-api-triagem-spring-rabbitmq"
)

for project in "${javaProjects[@]}"; do
    echo "-> Compilando $project..."
    pushd "$project" > /dev/null
    chmod +x mvnw
    ./mvnw clean package -DskipTests
    popd > /dev/null
done

echo "=============================================="
echo "2. Compilando o Frontend Angular..."
echo "=============================================="

pushd "healthcare-front-end" > /dev/null
echo "-> Instalando dependências e rodando build..."
npm install
npm run build
popd > /dev/null

echo "=============================================="
echo "3. Construindo as imagens Docker..."
echo "=============================================="

# Lista de imagens e seus diretórios
declare -A images
images["healthcare-gateway"]="api-spring-cloud-gateway-healthcaresys"
images["healthcare-api-pacientes"]="healthcare-api-pacientes-spring-rabbitmq"
images["healthcare-api-usuarios"]="healthcare-api-users-microservice-spring-rabbitmq"
images["healthcare-api-triagem"]="healthcare-api-triagem-spring-rabbitmq"
images["healthcare-front-end"]="healthcare-front-end"

for imgName in "${!images[@]}"; do
    dir="${images[$imgName]}"
    tagLocal="$imgName:latest"
    echo "-> docker build -t $tagLocal em $dir"
    docker build -t "$tagLocal" "$dir"

    if [ "$PUSH" = true ]; then
        # Converte nome de usuário para minúsculo
        userLower=$(echo "$GITHUB_USERNAME" | tr '[:upper:]' '[:lower:]')
        tagRegistry="$REGISTRY/$userLower/$imgName:latest"
        echo "-> docker tag $tagLocal $tagRegistry"
        docker tag "$tagLocal" "$tagRegistry"
        echo "-> docker push $tagRegistry"
        docker push "$tagRegistry"
    fi
done

echo "=============================================="
echo "Processo concluído com sucesso!"
echo "=============================================="
