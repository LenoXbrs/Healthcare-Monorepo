package br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioCacheDto(
        Long id,
        String nome,
        String role
) {}
