package br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Serviço de validação JWT — somente leitura.
 * A api-pacientes NÃO gera tokens; isso é responsabilidade exclusiva da api-usuarios.
 * Este serviço valida a assinatura e extrai claims do token gerado lá.
 *
 * Pré-requisito: jwt.secret DEVE ser o mesmo que o configurado na api-usuarios.
 */
@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Valida o token: verifica assinatura e expiração.
     *
     * @return true se o token for válido e não expirado
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JWT] Token inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrai o subject (email do usuário) do token.
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extrai a role do usuário, se presente nos claims.
     * Requer que a api-usuarios inclua "role" nos extraClaims ao gerar o token.
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
