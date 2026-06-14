package br.com.bali.code.healthcareapitriagem.Triagem.api.model.request;

import br.com.bali.code.healthcareapitriagem.Triagem.application.PrioridadeTriagem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ClassificarTriagemRequest(
        Long medicoId,
        @NotNull PrioridadeTriagem prioridade,
        @NotNull @Valid SinaisVitaisRequest sinaisVitais
) {}
