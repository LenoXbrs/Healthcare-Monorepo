package br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.integrations.rabbitmq.dto;

public record TriagemClassificadaPayload(
        Long triagemId,
        Long medicoId,
        String pacienteNome,
        String prioridade
) {}
