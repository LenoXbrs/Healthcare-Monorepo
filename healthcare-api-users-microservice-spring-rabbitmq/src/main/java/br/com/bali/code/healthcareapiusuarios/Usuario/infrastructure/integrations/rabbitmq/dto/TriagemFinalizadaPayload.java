package br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.integrations.rabbitmq.dto;

public record TriagemFinalizadaPayload(
        Long triagemId,
        Long pacienteId,
        String pacienteNome,
        Long enfermeiroId,
        Long medicoId,
        String status
) {}
