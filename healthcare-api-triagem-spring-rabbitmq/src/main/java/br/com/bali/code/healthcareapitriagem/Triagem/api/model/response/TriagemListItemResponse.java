package br.com.bali.code.healthcareapitriagem.Triagem.api.model.response;

import br.com.bali.code.healthcareapitriagem.Triagem.application.PrioridadeTriagem;
import br.com.bali.code.healthcareapitriagem.Triagem.application.StatusTriagem;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TriagemListItemResponse(
        Long id,
        Long pacienteId,
        StatusTriagem status,
        PrioridadeTriagem prioridade,
        LocalDateTime criadoEm,
        Boolean chamado,
        String salaChamada,
        LocalDateTime chamadoEm
) {}
