package br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.config;

import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rest.dto.PacienteCacheDto;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

/**
 * Grava dados do paciente no Redis quando o evento paciente.criado chega,
 * evitando chamada REST na classificação.
 */
@Service
public class PacienteCacheService {

    @CachePut(value = "paciente", key = "#dto.id()")
    public PacienteCacheDto cachear(PacienteCacheDto dto) {
        return dto;
    }
}
