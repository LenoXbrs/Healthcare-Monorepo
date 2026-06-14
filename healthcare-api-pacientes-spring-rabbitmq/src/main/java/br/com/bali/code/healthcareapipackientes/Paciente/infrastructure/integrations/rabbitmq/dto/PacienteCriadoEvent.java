package br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.integrations.rabbitmq.dto;

import br.com.bali.code.healthcareapipackientes.Paciente.application.StatusPaciente;

public record PacienteCriadoEvent(
        Long pacienteId,
        String nome,
        String cpf,
        StatusPaciente status
) {}
