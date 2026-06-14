package br.com.bali.code.healthcareapitriagem.Triagem.api.model.response;

import br.com.bali.code.healthcareapitriagem.Triagem.application.PrioridadeTriagem;
import br.com.bali.code.healthcareapitriagem.Triagem.application.StatusTriagem;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TriagemDetalheResponse(
        Long id,
        Long pacienteId,
        Long enfermeiroId,
        Long medicoId,
        StatusTriagem status,
        PrioridadeTriagem prioridade,
        String queixaPrincipal,
        LocalDateTime criadoEm,
        LocalDateTime classificadoEm,
        SinaisVitaisResponse sinaisVitais,
        Boolean chamado,
        String salaChamada,
        LocalDateTime chamadoEm
) {}
