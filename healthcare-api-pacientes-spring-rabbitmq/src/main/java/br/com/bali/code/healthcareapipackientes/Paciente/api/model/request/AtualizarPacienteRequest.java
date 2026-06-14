package br.com.bali.code.healthcareapipackientes.Paciente.api.model.request;

import br.com.bali.code.healthcareapipackientes.Paciente.application.StatusPaciente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtualizarPacienteRequest(
    @NotBlank(message = "Nome é obrigatório")
    String nome,

    @NotNull(message = "Status é obrigatório")
    StatusPaciente status,

    @NotNull(message = "Campo ativo é obrigatório")
    Boolean ativo
) {}
