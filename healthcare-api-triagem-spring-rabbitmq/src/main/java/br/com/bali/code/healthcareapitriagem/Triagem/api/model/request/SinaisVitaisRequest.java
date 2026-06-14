package br.com.bali.code.healthcareapitriagem.Triagem.api.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SinaisVitaisRequest(
        @NotNull @Positive Integer pressaoSist,
        @NotNull @Positive Integer pressaoDiast,
        @NotNull BigDecimal temperatura,
        @NotNull @Positive Integer frequencia,
        @NotNull @Positive Integer saturacao
) {}
