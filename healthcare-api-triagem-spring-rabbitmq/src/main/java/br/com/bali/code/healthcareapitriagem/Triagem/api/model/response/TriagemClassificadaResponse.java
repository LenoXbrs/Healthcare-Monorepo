package br.com.bali.code.healthcareapitriagem.Triagem.api.model.response;

import br.com.bali.code.healthcareapitriagem.Triagem.application.PrioridadeTriagem;
import br.com.bali.code.healthcareapitriagem.Triagem.application.StatusTriagem;

public record TriagemClassificadaResponse(
        Long id,
        PrioridadeTriagem prioridade,
        Long medicoId,
        StatusTriagem status
) {}
