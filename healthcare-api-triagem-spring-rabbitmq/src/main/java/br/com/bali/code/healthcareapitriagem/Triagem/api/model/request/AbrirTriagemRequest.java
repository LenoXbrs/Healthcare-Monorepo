package br.com.bali.code.healthcareapitriagem.Triagem.api.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AbrirTriagemRequest(
        @NotNull Long pacienteId,
        @NotNull Long enfermeiroId,
        @NotBlank String queixaPrincipal
) {}
