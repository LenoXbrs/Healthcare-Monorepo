package br.com.bali.code.healthcareapitriagem.Triagem.api.model.response;

import br.com.bali.code.healthcareapitriagem.Triagem.application.StatusTriagem;

public record TriagemFinalizadaResponse(
        Long id,
        StatusTriagem status
) {}
