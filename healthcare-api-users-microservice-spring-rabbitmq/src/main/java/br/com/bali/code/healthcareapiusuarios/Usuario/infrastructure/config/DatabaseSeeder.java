package br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.config;

import br.com.bali.code.healthcareapiusuarios.Usuario.application.Role;
import br.com.bali.code.healthcareapiusuarios.Usuario.domain.model.User;
import br.com.bali.code.healthcareapiusuarios.Usuario.domain.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            // Seed Admin
            userRepository.save(User.builder()
                    .name("Administrador")
                    .email("admin@healthcare.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .active(true)
                    .build());

            // Seed Enfermeiro
            userRepository.save(User.builder()
                    .name("Enfermeiro Teste")
                    .email("enfermeiro@healthcare.com")
                    .password(passwordEncoder.encode("enfermeiro123"))
                    .role(Role.ENFERMEIRO)
                    .active(true)
                    .build());

            // Seed Medicos
            userRepository.save(User.builder()
                    .name("Dr. Roberto")
                    .email("roberto@healthcare.com")
                    .password(passwordEncoder.encode("medico123"))
                    .role(Role.MEDICO)
                    .active(true)
                    .build());

            userRepository.save(User.builder()
                    .name("Dr. Amanda")
                    .email("amanda@healthcare.com")
                    .password(passwordEncoder.encode("medico123"))
                    .role(Role.MEDICO)
                    .active(true)
                    .build());

            // Seed Recepcionista
            userRepository.save(User.builder()
                    .name("Recepcionista Teste")
                    .email("recepcionista@healthcare.com")
                    .password(passwordEncoder.encode("recepcionista123"))
                    .role(Role.RECEPCIONISTA)
                    .active(true)
                    .build());

            System.out.println(">>> Banco de dados inicializado com usuários padrão!");
        }
    }
}
