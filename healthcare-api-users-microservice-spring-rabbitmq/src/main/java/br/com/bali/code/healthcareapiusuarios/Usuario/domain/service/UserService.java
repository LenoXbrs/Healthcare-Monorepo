package br.com.bali.code.healthcareapiusuarios.Usuario.domain.service;

import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.request.LoginRequest;
import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.request.RegisterUserRequest;
import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.request.UpdateUserRequest;
import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.response.LoginResponse;
import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.response.RegisterUserResponse;
import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.response.UserResponse;
import br.com.bali.code.healthcareapiusuarios.Usuario.application.Role;

import java.util.List;

public interface UserService {
    RegisterUserResponse register(RegisterUserRequest request);
    LoginResponse login(LoginRequest request);
    UserResponse getById(Long id);
    List<UserResponse> listByRole(Role role);
    UserResponse update(Long id, UpdateUserRequest request);
}
