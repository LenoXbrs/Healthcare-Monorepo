package br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.integrations.rabbitmq.dto;

import java.time.LocalDateTime;

public record TriagemCriadaPayload(
        Long triagemId,
        Long pacienteId,
        String pacienteNome,
        String status,
        LocalDateTime criadoEm
) {}
