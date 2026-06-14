package br.com.bali.code.healthcareapiusuarios.Usuario.api.model.response;

import br.com.bali.code.healthcareapiusuarios.Usuario.application.Role;

public record LoginResponse(
    String token,
    Long usuarioId,
    String nome,
    Role role
) {}
