package br.com.bali.code.healthcareapiusuarios.Usuario.domain.service;

import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.request.LoginRequest;
import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.request.RegisterUserRequest;
import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.request.UpdateUserRequest;
import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.response.LoginResponse;
import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.response.RegisterUserResponse;
import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.response.UserResponse;
import br.com.bali.code.healthcareapiusuarios.Usuario.application.Role;
import br.com.bali.code.healthcareapiusuarios.Usuario.domain.model.User;
import br.com.bali.code.healthcareapiusuarios.Usuario.domain.repository.UserRepository;
import br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.security.JwtService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;

    public UserServiceImp(UserRepository userRepository, 
                          PasswordEncoder passwordEncoder, 
                          JwtService jwtService, 
                          StringRedisTemplate redisTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public RegisterUserResponse register(RegisterUserRequest request) {
        User user = User.builder()
                .name(request.nome())
                .email(request.email())
                .password(passwordEncoder.encode(request.senha()))
                .role(request.role())
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        return new RegisterUserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getCreateAt()
        );
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.senha(), user.getPassword())) {
            throw new RuntimeException("Senha inválida");
        }

        // Inclui a role nos claims para que outros serviços possam validar
        // sem precisar chamar a api-usuarios (princípio de autonomia)
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());

        String token = jwtService.generateToken(extraClaims, user.getEmail());

        // Guardar no Redis com TTL de 1h
        redisTemplate.opsForValue().set("token:" + user.getEmail(), token, 1, TimeUnit.HOURS);

        return new LoginResponse(
                token,
                user.getId(),
                user.getName(),
                user.getRole()
        );
    }

    @Override
    @Cacheable(value = "usuarios", key = "#id")
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getActive()
        );
    }

    @Override
    @Cacheable(value = "medicos", key = "#role")
    public List<UserResponse> listByRole(Role role) {
        return userRepository.findByRoleAndActiveTrue(role).stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getName(),
                        null, // User only wants id, nome, role in listing
                        user.getRole(),
                        user.getActive()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "usuarios", key = "#id")
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        user.setName(request.nome());
        user.setEmail(request.email());
        user.setRole(request.role());
        user.setActive(request.ativo());

        User updatedUser = userRepository.save(user);

        return new UserResponse(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getRole(),
                null
        );
    }
}
