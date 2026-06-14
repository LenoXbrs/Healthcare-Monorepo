package br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/health",
                    "/actuator/health/**",
                    "/actuator/prometheus",
                    "/actuator/info"
                ).permitAll()

                .requestMatchers(HttpMethod.POST, "/triagem/check-in")
                    .hasAnyRole("RECEPCIONISTA", "ENFERMEIRO", "ADMIN")

                .requestMatchers(HttpMethod.POST, "/triagem")
                    .hasRole("ENFERMEIRO")

                .requestMatchers(HttpMethod.POST, "/triagem/*/classificar")
                    .hasAnyRole("ENFERMEIRO", "MEDICO")

                .requestMatchers(HttpMethod.GET, "/triagem/**")
                    .hasAnyRole("MEDICO", "ENFERMEIRO")

                .requestMatchers(HttpMethod.PUT, "/triagem/*/finalizar")
                    .hasAnyRole("MEDICO", "ENFERMEIRO")

                .requestMatchers(HttpMethod.POST, "/diagnosticos")
                    .hasRole("MEDICO")

                .requestMatchers(HttpMethod.PUT, "/diagnosticos/*")
                    .hasRole("MEDICO")

                .requestMatchers(HttpMethod.POST, "/diagnosticos/*/assinar")
                    .hasRole("MEDICO")

                .requestMatchers(HttpMethod.GET, "/diagnosticos/*/pdf")
                    .hasAnyRole("MEDICO", "ENFERMEIRO", "ADMIN")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
