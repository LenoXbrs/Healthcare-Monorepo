package br.com.bali.code.healthcareapitriagem.Triagem.api.model.request;

import jakarta.validation.constraints.NotBlank;

public record AssinarDiagnosticoRequest(
        @NotBlank(message = "Assinatura desenhada é obrigatória")
        String assinaturaBase64
) {}
