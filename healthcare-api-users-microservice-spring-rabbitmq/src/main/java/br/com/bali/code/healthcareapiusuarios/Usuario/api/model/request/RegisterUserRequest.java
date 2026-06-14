package br.com.bali.code.healthcareapiusuarios.Usuario.api.model.request;

import br.com.bali.code.healthcareapiusuarios.Usuario.application.Role;

public record RegisterUserRequest(String nome, String email, String senha, Role role) {
}
