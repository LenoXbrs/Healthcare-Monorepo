package br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.dto;

/**
 * Espelha o payload publicado pela api-pacientes em paciente.criado.
 */
public record PacienteCriadoEvent(
        Long pacienteId,
        String nome,
        String cpf,
        String status
) {}
