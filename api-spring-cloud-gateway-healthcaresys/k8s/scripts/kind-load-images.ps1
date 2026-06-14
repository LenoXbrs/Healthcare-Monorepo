# Carrega imagens locais no cluster kind (execute após build-images.ps1)
$ErrorActionPreference = "Stop"

$images = @(
    "healthcare-gateway:latest",
    "healthcare-api-pacientes:latest",
    "healthcare-api-usuarios:latest",
    "healthcare-api-triagem:latest"
)

foreach ($img in $images) {
    Write-Host "kind load docker-image $img"
    kind load docker-image $img
}

Write-Host "Imagens carregadas no kind."
