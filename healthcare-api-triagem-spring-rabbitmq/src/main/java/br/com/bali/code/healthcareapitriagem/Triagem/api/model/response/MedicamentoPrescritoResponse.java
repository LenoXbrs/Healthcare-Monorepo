package br.com.bali.code.healthcareapitriagem.Triagem.api.model.response;

public record MedicamentoPrescritoResponse(
        Long id,
        String nome,
        String dosagem,
        String frequencia,
        String prazoUso
) {}
