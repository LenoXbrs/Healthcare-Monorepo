package br.com.bali.code.healthcareapitriagem.Triagem.api.model.response;

import br.com.bali.code.healthcareapitriagem.Triagem.application.StatusTriagem;

import java.time.LocalDateTime;

public record TriagemAbertaResponse(
        Long id,
        Long pacienteId,
        StatusTriagem status,
        LocalDateTime criadoEm
) {}
