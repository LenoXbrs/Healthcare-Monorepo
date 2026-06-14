package br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.dto;

/**
 * Compatível com TriagemClassificadaPayload da api-usuarios.
 */
public record TriagemClassificadaEvent(
        Long triagemId,
        Long medicoId,
        String pacienteNome,
        String prioridade
) {}
