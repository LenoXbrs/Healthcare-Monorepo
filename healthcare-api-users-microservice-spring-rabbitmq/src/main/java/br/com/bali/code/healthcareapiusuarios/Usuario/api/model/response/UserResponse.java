package br.com.bali.code.healthcareapiusuarios.Usuario.api.model.response;

import br.com.bali.code.healthcareapiusuarios.Usuario.application.Role;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
    Long id,
    String nome,
    String email,
    Role role,
    Boolean ativo
) {}
