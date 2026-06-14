package br.com.bali.code.healthcareapiusuarios.Usuario.api.model.response;

import br.com.bali.code.healthcareapiusuarios.Usuario.application.Role;
import java.time.LocalDateTime;

public record RegisterUserResponse(
        Long id,
        String nome,
        String email,
        Role role,
        LocalDateTime criadoEm) {
}
