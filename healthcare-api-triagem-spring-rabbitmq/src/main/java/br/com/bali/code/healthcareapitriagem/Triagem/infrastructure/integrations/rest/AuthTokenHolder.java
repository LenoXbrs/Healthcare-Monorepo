package br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rest;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Propaga o JWT da requisição atual para chamadas REST síncronas.
 */
@Component
@RequestScope
public class AuthTokenHolder {

    private String bearerToken;

    public void setBearerToken(String token) {
        this.bearerToken = token;
    }

    public String getBearerToken() {
        return bearerToken;
    }
}
