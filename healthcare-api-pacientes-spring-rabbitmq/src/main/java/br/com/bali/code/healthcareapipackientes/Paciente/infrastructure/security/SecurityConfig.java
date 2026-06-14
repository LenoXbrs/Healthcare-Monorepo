package br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração de segurança — Defesa em profundidade (Opção B).
 *
 * Mesmo que o API Gateway já valide o JWT, este serviço revalida
 * a assinatura e aplica controle de acesso por role.
 *
 * Matriz de acesso:
 * ┌───────────────────────────────────┬────────────────────────────────────────┐
 * │ Endpoint                          │ Roles permitidas                       │
 * ├───────────────────────────────────┼────────────────────────────────────────┤
 * │ GET /pacientes/{id}               │ MEDICO, ENFERMEIRO, RECEPCIONISTA      │
 * │ GET /pacientes/cpf/{cpf}          │ MEDICO, ENFERMEIRO, RECEPCIONISTA      │
 * │ GET /pacientes?status=            │ MEDICO, ENFERMEIRO                     │
 * │ PUT /pacientes/{id}               │ MEDICO, ENFERMEIRO                     │
 * │ GET /swagger-ui, /v3/api-docs     │ Público (dev/docs)                     │
 * │ GET /actuator/health              │ Público (health check do orquestrador) │
 * └───────────────────────────────────┴────────────────────────────────────────┘
 *
 * Sessão: STATELESS — JWT é o único mecanismo de autenticação.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth

                // ── Rotas públicas ────────────────────────────────────────
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/health",
                    "/actuator/health/**",
                    "/actuator/prometheus",
                    "/actuator/info"
                ).permitAll()

                // ── GET por ID e CPF — qualquer profissional ─────────────
                .requestMatchers(HttpMethod.GET, "/pacientes/{id}")
                    .hasAnyRole("ADMIN", "MEDICO", "ENFERMEIRO", "RECEPCIONISTA")

                .requestMatchers(HttpMethod.GET, "/pacientes/cpf/{cpf}")
                    .hasAnyRole("ADMIN", "MEDICO", "ENFERMEIRO", "RECEPCIONISTA")

                // ── GET por status — apenas equipe clínica ─────────────
                .requestMatchers(HttpMethod.GET, "/pacientes")
                    .hasAnyRole("ADMIN", "MEDICO", "ENFERMEIRO")

                // ── POST — criar paciente ─────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/pacientes")
                    .hasAnyRole("ADMIN", "RECEPCIONISTA", "ENFERMEIRO")

                // ── PUT — apenas quem pode alterar status clínico ────────
                .requestMatchers(HttpMethod.PUT, "/pacientes/{id}")
                    .hasAnyRole("ADMIN", "MEDICO", "ENFERMEIRO")

                // ── Qualquer outra rota exige autenticação ───────────────
                .anyRequest().authenticated()
            )
            // Registra o filtro JWT antes do filtro de autenticação padrão
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
