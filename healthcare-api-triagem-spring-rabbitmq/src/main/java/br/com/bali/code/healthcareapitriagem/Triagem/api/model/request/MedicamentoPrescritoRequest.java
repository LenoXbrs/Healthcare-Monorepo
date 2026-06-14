package br.com.bali.code.healthcareapitriagem.Triagem.api.model.request;

import jakarta.validation.constraints.NotBlank;

public record MedicamentoPrescritoRequest(
        @NotBlank String nome,
        @NotBlank String dosagem,
        @NotBlank String frequencia,
        @NotBlank String prazoUso
) {}
