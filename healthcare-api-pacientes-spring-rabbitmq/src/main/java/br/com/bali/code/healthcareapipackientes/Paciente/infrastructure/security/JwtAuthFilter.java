package br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro JWT executado uma vez por requisição.
 *
 * Fluxo:
 * 1. Extrai o token do header Authorization: Bearer <token>
 * 2. Valida assinatura e expiração via JwtService
 * 3. Extrai email e role dos claims
 * 4. Popula o SecurityContext com a autenticação e as authorities
 *
 * Se o token for inválido/ausente: SecurityContext fica vazio,
 * e o Spring Security retorna 401 para rotas protegidas.
 *
 * O role é prefixado com ROLE_ conforme convenção do Spring Security.
 */
@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Sem header ou formato inválido — deixa o Spring Security tratar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        // Evita reprocessar se já autenticado nesta thread
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtService.isTokenValid(token)) {
            log.warn("[JWT] Token rejeitado para {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtService.extractEmail(token);
        String role  = jwtService.extractRole(token);

        // Se a api-usuarios não incluir role no token, nega acesso por segurança
        if (role == null || role.isBlank()) {
            log.warn("[JWT] Token válido mas sem claim 'role' para usuário: {}", email);
            filterChain.doFilter(request, response);
            return;
        }

        // Popula SecurityContext — Spring Security usa "ROLE_" como prefixo padrão
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        var authentication = new UsernamePasswordAuthenticationToken(
                email,
                null,   // credentials nulas — sem senha neste ponto
                authorities
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("[JWT] Usuário autenticado: {} | role: {}", email, role);

        filterChain.doFilter(request, response);
    }
}
