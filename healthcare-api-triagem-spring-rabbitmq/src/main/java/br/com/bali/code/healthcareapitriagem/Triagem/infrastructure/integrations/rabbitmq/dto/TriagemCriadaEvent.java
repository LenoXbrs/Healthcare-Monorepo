package br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.dto;

import java.time.LocalDateTime;

public record TriagemCriadaEvent(
        Long triagemId,
        Long pacienteId,
        String pacienteNome,
        String status,
        LocalDateTime criadoEm
) {}
