package br.com.bali.code.healthcareapitriagem.Triagem.domain.service;

import br.com.bali.code.healthcareapitriagem.Triagem.api.model.response.MedicoSalaResponse;
import br.com.bali.code.healthcareapitriagem.Triagem.domain.model.MedicoSala;
import br.com.bali.code.healthcareapitriagem.Triagem.domain.repository.MedicoSalaRepository;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rest.UsuarioClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicoSalaServiceImp implements MedicoSalaService {

    private final MedicoSalaRepository medicoSalaRepository;
    private final UsuarioClient usuarioClient;

    public MedicoSalaServiceImp(MedicoSalaRepository medicoSalaRepository, UsuarioClient usuarioClient) {
        this.medicoSalaRepository = medicoSalaRepository;
        this.usuarioClient = usuarioClient;
    }

    @Override
    @Transactional
    public MedicoSalaResponse salvarOuAtualizar(Long medicoId, String sala) {
        // Validar se o usuário existe e é de fato um médico
        usuarioClient.validarMedico(medicoId);

        MedicoSala medicoSala = medicoSalaRepository.findByMedicoId(medicoId)
                .orElseGet(() -> MedicoSala.builder().medicoId(medicoId).build());

        medicoSala.setSala(sala);
        MedicoSala salvo = medicoSalaRepository.save(medicoSala);

        return new MedicoSalaResponse(salvo.getMedicoId(), salvo.getSala());
    }

    @Override
    public MedicoSalaResponse buscarPorMedicoId(Long medicoId) {
        return medicoSalaRepository.findByMedicoId(medicoId)
                .map(m -> new MedicoSalaResponse(m.getMedicoId(), m.getSala()))
                .orElse(new MedicoSalaResponse(medicoId, null));
    }

    @Override
    public List<MedicoSalaResponse> listarTodos() {
        return medicoSalaRepository.findAll().stream()
                .map(m -> new MedicoSalaResponse(m.getMedicoId(), m.getSala()))
                .toList();
    }

    @Override
    @Transactional
    public void remover(Long medicoId) {
        medicoSalaRepository.findByMedicoId(medicoId)
                .ifPresent(medicoSalaRepository::delete);
    }
}
