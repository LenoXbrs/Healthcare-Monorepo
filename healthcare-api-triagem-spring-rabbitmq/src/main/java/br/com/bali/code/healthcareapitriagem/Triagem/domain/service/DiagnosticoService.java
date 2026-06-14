package br.com.bali.code.healthcareapitriagem.Triagem.domain.service;

import br.com.bali.code.healthcareapitriagem.Triagem.api.model.request.SalvarDiagnosticoRequest;
import br.com.bali.code.healthcareapitriagem.Triagem.api.model.response.DiagnosticoResponse;

public interface DiagnosticoService {

    DiagnosticoResponse iniciar(Long triagemId, Long medicoId);

    DiagnosticoResponse salvar(Long id, SalvarDiagnosticoRequest request, Long medicoId);

    DiagnosticoResponse assinar(Long id, Long medicoId, String assinaturaBase64);

    byte[] gerarPdf(Long id);
}
