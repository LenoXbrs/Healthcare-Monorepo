package br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.dto;

public record TriagemFinalizadaEvent(
        Long triagemId,
        Long pacienteId,
        String pacienteNome,
        Long enfermeiroId,
        Long medicoId,
        String status
) {}
