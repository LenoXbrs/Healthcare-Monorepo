package br.com.bali.code.healthcareapitriagem.Triagem.api.model.request;

import java.util.List;

public record SalvarDiagnosticoRequest(
        String descricao,
        List<MedicamentoPrescritoRequest> medicamentos
) {}
