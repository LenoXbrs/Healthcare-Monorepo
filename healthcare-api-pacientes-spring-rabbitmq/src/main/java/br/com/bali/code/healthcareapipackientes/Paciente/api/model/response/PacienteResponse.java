package br.com.bali.code.healthcareapipackientes.Paciente.api.model.response;

import br.com.bali.code.healthcareapipackientes.Paciente.application.StatusPaciente;
import com.fasterxml.jackson.annotation.JsonInclude;


import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PacienteResponse(
    Long id,
    String nome,
    String cpf,
    StatusPaciente status,
    Boolean ativo,
    LocalDateTime criadoEm
) {}
