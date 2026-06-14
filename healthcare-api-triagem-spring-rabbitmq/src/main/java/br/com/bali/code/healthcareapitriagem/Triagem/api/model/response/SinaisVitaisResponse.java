package br.com.bali.code.healthcareapitriagem.Triagem.api.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SinaisVitaisResponse(
        Integer pressaoSist,
        Integer pressaoDiast,
        BigDecimal temperatura,
        Integer frequencia,
        Integer saturacao,
        LocalDateTime coletadoEm
) {}
