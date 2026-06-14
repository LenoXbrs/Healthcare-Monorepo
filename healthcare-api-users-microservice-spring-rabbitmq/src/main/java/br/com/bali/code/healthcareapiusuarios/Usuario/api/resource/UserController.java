package br.com.bali.code.healthcareapiusuarios.Usuario.api.resource;

import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.request.LoginRequest;
import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.request.RegisterUserRequest;
import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.request.UpdateUserRequest;
import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.response.LoginResponse;
import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.response.RegisterUserResponse;
import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.response.UserResponse;
import br.com.bali.code.healthcareapiusuarios.Usuario.application.Role;
import br.com.bali.code.healthcareapiusuarios.Usuario.domain.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<RegisterUserResponse> register(@RequestBody @Valid RegisterUserRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listByRole(@RequestParam Role role) {
        return ResponseEntity.ok(userService.listByRole(role));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody @Valid UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }
}
