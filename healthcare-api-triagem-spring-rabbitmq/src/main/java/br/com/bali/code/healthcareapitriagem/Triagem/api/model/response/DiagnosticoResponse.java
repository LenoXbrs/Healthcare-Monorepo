package br.com.bali.code.healthcareapitriagem.Triagem.api.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DiagnosticoResponse(
        Long id,
        Long triagemId,
        Long medicoId,
        String descricao,
        String status,
        String assinaturaHash,
        String assinaturaBase64,
        LocalDateTime assinadoEm,
        List<MedicamentoPrescritoResponse> medicamentos,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
