package br.com.bali.code.healthcareapitriagem.Triagem.domain.service;

import br.com.bali.code.healthcareapitriagem.Triagem.api.model.request.AbrirTriagemRequest;
import br.com.bali.code.healthcareapitriagem.Triagem.api.model.request.ClassificarTriagemRequest;
import br.com.bali.code.healthcareapitriagem.Triagem.api.model.request.FinalizarTriagemRequest;
import br.com.bali.code.healthcareapitriagem.Triagem.api.model.request.SinaisVitaisRequest;
import br.com.bali.code.healthcareapitriagem.Triagem.api.model.response.*;
import br.com.bali.code.healthcareapitriagem.Triagem.application.PrioridadeTriagem;
import br.com.bali.code.healthcareapitriagem.Triagem.application.StatusTriagem;
import br.com.bali.code.healthcareapitriagem.Triagem.domain.model.SinaisVitais;
import br.com.bali.code.healthcareapitriagem.Triagem.domain.model.Triagem;
import br.com.bali.code.healthcareapitriagem.Triagem.domain.repository.TriagemRepository;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.config.PacienteCacheService;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.exception.TriagemNaoEncontradaException;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.dto.PacienteCriadoEvent;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.dto.TriagemClassificadaEvent;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.dto.TriagemCriadaEvent;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.dto.TriagemFinalizadaEvent;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.publisher.TriagemEventPublisher;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rest.PacienteClient;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rest.UsuarioClient;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rest.dto.PacienteCacheDto;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rest.dto.UsuarioCacheDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class TriagemServiceImp implements TriagemService {

    private final TriagemRepository triagemRepository;
    private final TriagemEventPublisher eventPublisher;
    private final PacienteCacheService pacienteCacheService;
    private final UsuarioClient usuarioClient;
    private final PacienteClient pacienteClient;

    public TriagemServiceImp(
            TriagemRepository triagemRepository,
            TriagemEventPublisher eventPublisher,
            PacienteCacheService pacienteCacheService,
            UsuarioClient usuarioClient,
            PacienteClient pacienteClient) {
        this.triagemRepository = triagemRepository;
        this.eventPublisher = eventPublisher;
        this.pacienteCacheService = pacienteCacheService;
        this.usuarioClient = usuarioClient;
        this.pacienteClient = pacienteClient;
    }

    @Override
    @Transactional
    public void criarTriagemFromPaciente(PacienteCriadoEvent event) {
        if (triagemRepository.existsByPacienteIdAndStatusNot(event.pacienteId(), StatusTriagem.FINALIZADO)) {
            log.warn("[paciente.criado] Triagem ativa já existe para pacienteId={}", event.pacienteId());
            return;
        }

        Triagem triagem = Triagem.builder()
                .pacienteId(event.pacienteId())
                .status(StatusTriagem.AGUARDANDO)
                .build();

        Triagem salva = triagemRepository.save(triagem);

        pacienteCacheService.cachear(new PacienteCacheDto(
                event.pacienteId(),
                event.nome(),
                event.cpf()
        ));

        eventPublisher.publicarCriada(new TriagemCriadaEvent(
                salva.getId(),
                salva.getPacienteId(),
                event.nome(),
                salva.getStatus().name(),
                salva.getCreatedAt()
        ));

        log.info("[paciente.criado] Triagem criada: triagemId={} pacienteId={}",
                salva.getId(), salva.getPacienteId());
    }

    @Override
    @Transactional
    public TriagemAbertaResponse abrir(AbrirTriagemRequest request) {
        Triagem triagem = triagemRepository.findByPacienteIdAndStatus(request.pacienteId(), StatusTriagem.AGUARDANDO)
                .orElseThrow(() -> new TriagemNaoEncontradaException(
                        "Nenhuma triagem aguardando atendimento encontrada para o paciente " + request.pacienteId()));

        if (triagem.getStatus() != StatusTriagem.AGUARDANDO) {
            throw new IllegalArgumentException("Triagem não está aguardando atendimento");
        }

        usuarioClient.validarEnfermeiro(request.enfermeiroId());

        triagem.setEnfermeiroId(request.enfermeiroId());
        triagem.setQueixaPrincipal(request.queixaPrincipal());

        Triagem salva = triagemRepository.save(triagem);

        return new TriagemAbertaResponse(
                salva.getId(),
                salva.getPacienteId(),
                salva.getStatus(),
                salva.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public TriagemClassificadaResponse classificar(Long id, ClassificarTriagemRequest request) {
        Triagem triagem = triagemRepository.findById(id)
                .orElseThrow(() -> new TriagemNaoEncontradaException(id));

        if (triagem.getStatus() != StatusTriagem.AGUARDANDO) {
            throw new IllegalArgumentException("Triagem não pode ser classificada no status atual");
        }
        if (triagem.getEnfermeiroId() == null || triagem.getQueixaPrincipal() == null) {
            throw new IllegalArgumentException("Triagem ainda não foi aberta pelo enfermeiro");
        }

        Long medicoId = request.medicoId();
        if (medicoId == null) {
            List<UsuarioCacheDto> medicosAtivos = usuarioClient.listarMedicos();
            if (medicosAtivos.isEmpty()) {
                throw new IllegalArgumentException("Nenhum médico ativo cadastrado no sistema.");
            }
            Long medicoIdSelecionado = null;
            long menorCarga = Long.MAX_VALUE;
            for (UsuarioCacheDto medico : medicosAtivos) {
                long carga = triagemRepository.countByMedicoIdAndStatus(medico.id(), StatusTriagem.EM_ATENDIMENTO);
                if (carga < menorCarga) {
                    menorCarga = carga;
                    medicoIdSelecionado = medico.id();
                }
            }
            medicoId = medicoIdSelecionado;
            log.info("[triagem.roteamento] Direcionamento automático: triagemId={} atribuída ao medicoId={} (carga={})",
                    id, medicoId, menorCarga);
        } else {
            usuarioClient.validarMedico(medicoId);
        }

        SinaisVitaisRequest sv = request.sinaisVitais();
        SinaisVitais sinaisVitais = SinaisVitais.builder()
                .triagem(triagem)
                .pressaoSist(sv.pressaoSist())
                .pressaoDiast(sv.pressaoDiast())
                .temperatura(sv.temperatura())
                .frequencia(sv.frequencia())
                .saturacao(sv.saturacao())
                .coletadoEm(LocalDateTime.now())
                .build();

        triagem.setSinaisVitais(sinaisVitais);
        triagem.setMedicoId(medicoId);
        triagem.setPrioridade(request.prioridade());
        triagem.setStatus(StatusTriagem.EM_ATENDIMENTO);
        triagem.setClassificadoEm(LocalDateTime.now());

        Triagem salva = triagemRepository.save(triagem);

        String pacienteNome = resolverNomePaciente(salva.getPacienteId());

        eventPublisher.publicarClassificada(new TriagemClassificadaEvent(
                salva.getId(),
                salva.getMedicoId(),
                pacienteNome,
                salva.getPrioridade().name()
        ));

        return new TriagemClassificadaResponse(
                salva.getId(),
                salva.getPrioridade(),
                salva.getMedicoId(),
                salva.getStatus()
        );
    }

    @Override
    public List<TriagemListItemResponse> listar() {
        return triagemRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toListItem)
                .toList();
    }

    @Override
    public TriagemDetalheResponse buscarPorId(Long id) {
        Triagem triagem = triagemRepository.findWithSinaisVitaisById(id)
                .orElseThrow(() -> new TriagemNaoEncontradaException(id));
        return toDetalhe(triagem);
    }

    @Override
    @Transactional
    public TriagemFinalizadaResponse finalizar(Long id, FinalizarTriagemRequest request) {
        Triagem triagem = triagemRepository.findById(id)
                .orElseThrow(() -> new TriagemNaoEncontradaException(id));

        if (triagem.getStatus() != StatusTriagem.EM_ATENDIMENTO) {
            throw new IllegalArgumentException("Triagem não está em atendimento");
        }

        triagem.setStatus(StatusTriagem.FINALIZADO);
        triagem.setObservacoes(request.observacoes());

        Triagem salva = triagemRepository.save(triagem);

        eventPublisher.publicarFinalizada(new TriagemFinalizadaEvent(
                salva.getId(),
                salva.getPacienteId(),
                resolverNomePaciente(salva.getPacienteId()),
                salva.getEnfermeiroId(),
                salva.getMedicoId(),
                salva.getStatus().name()
        ));

        return new TriagemFinalizadaResponse(salva.getId(), salva.getStatus());
    }

    private String resolverNomePaciente(Long pacienteId) {
        try {
            return pacienteClient.buscarPorId(pacienteId).nome();
        } catch (Exception e) {
            log.warn("Nome do paciente {} não encontrado no cache/REST", pacienteId);
            return "Paciente";
        }
    }

    private TriagemListItemResponse toListItem(Triagem t) {
        return new TriagemListItemResponse(
                t.getId(),
                t.getPacienteId(),
                t.getStatus(),
                t.getPrioridade(),
                t.getCreatedAt(),
                t.getChamado(),
                t.getSalaChamada(),
                t.getChamadoEm()
        );
    }

    private TriagemDetalheResponse toDetalhe(Triagem t) {
        SinaisVitaisResponse sv = null;
        if (t.getSinaisVitais() != null) {
            SinaisVitais s = t.getSinaisVitais();
            sv = new SinaisVitaisResponse(
                    s.getPressaoSist(),
                    s.getPressaoDiast(),
                    s.getTemperatura(),
                    s.getFrequencia(),
                    s.getSaturacao(),
                    s.getColetadoEm()
            );
        }
        return new TriagemDetalheResponse(
                t.getId(),
                t.getPacienteId(),
                t.getEnfermeiroId(),
                t.getMedicoId(),
                t.getStatus(),
                t.getPrioridade(),
                t.getQueixaPrincipal(),
                t.getCreatedAt(),
                t.getClassificadoEm(),
                sv,
                t.getChamado(),
                t.getSalaChamada(),
                t.getChamadoEm()
        );
    }

    @Override
    @Transactional
    public TriagemListItemResponse checkIn(Long pacienteId) {
        if (triagemRepository.existsByPacienteIdAndStatusNot(pacienteId, StatusTriagem.FINALIZADO)) {
            throw new IllegalArgumentException("O paciente já possui uma triagem ativa em andamento.");
        }

        var paciente = pacienteClient.buscarPorId(pacienteId);

        Triagem triagem = Triagem.builder()
                .pacienteId(pacienteId)
                .status(StatusTriagem.AGUARDANDO)
                .build();

        Triagem salva = triagemRepository.save(triagem);

        eventPublisher.publicarCriada(new br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.dto.TriagemCriadaEvent(
                salva.getId(),
                salva.getPacienteId(),
                paciente.nome(),
                salva.getStatus().name(),
                salva.getCreatedAt()
        ));

        log.info("[paciente.check-in] Triagem de check-in criada: triagemId={} pacienteId={} nome={}",
                salva.getId(), salva.getPacienteId(), paciente.nome());

        return toListItem(salva);
    }

    @Override
    @Transactional
    public TriagemDetalheResponse chamar(Long id, String sala) {
        Triagem triagem = triagemRepository.findById(id)
                .orElseThrow(() -> new TriagemNaoEncontradaException(id));

        if (triagem.getStatus() == StatusTriagem.FINALIZADO) {
            throw new IllegalArgumentException("Não é possível chamar um paciente de uma triagem finalizada.");
        }

        triagem.setChamado(true);
        triagem.setSalaChamada(sala);
        triagem.setChamadoEm(LocalDateTime.now());

        Triagem salva = triagemRepository.save(triagem);
        log.info("[triagem.chamar] Paciente chamado: triagemId={} sala={}", salva.getId(), sala);

        return toDetalhe(salva);
    }
}
