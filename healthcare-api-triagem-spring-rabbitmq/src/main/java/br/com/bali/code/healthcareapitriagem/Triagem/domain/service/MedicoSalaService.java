package br.com.bali.code.healthcareapitriagem.Triagem.domain.service;

import br.com.bali.code.healthcareapitriagem.Triagem.api.model.response.MedicoSalaResponse;
import java.util.List;

public interface MedicoSalaService {
    MedicoSalaResponse salvarOuAtualizar(Long medicoId, String sala);
    MedicoSalaResponse buscarPorMedicoId(Long medicoId);
    List<MedicoSalaResponse> listarTodos();
    void remover(Long medicoId);
}
