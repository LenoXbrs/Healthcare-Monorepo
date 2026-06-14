package br.com.bali.code.healthcareapipackientes.Paciente.api.model.request;

import jakarta.validation.constraints.NotBlank;

public record CriarPacienteRequest(@NotBlank(message = "Nome é obrigatório")
                                   String nome,
                                   @NotBlank(message = "CPF é obrigatório")
                                   String cpf) {
}
