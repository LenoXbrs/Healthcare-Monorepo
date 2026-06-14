package br.com.bali.code.healthcareapitriagem.Triagem.domain.service;

import br.com.bali.code.healthcareapitriagem.Triagem.api.model.request.SalvarDiagnosticoRequest;
import br.com.bali.code.healthcareapitriagem.Triagem.api.model.response.DiagnosticoResponse;
import br.com.bali.code.healthcareapitriagem.Triagem.api.model.response.MedicamentoPrescritoResponse;
import br.com.bali.code.healthcareapitriagem.Triagem.application.StatusTriagem;
import br.com.bali.code.healthcareapitriagem.Triagem.domain.model.Diagnostico;
import br.com.bali.code.healthcareapitriagem.Triagem.domain.model.MedicamentoPrescrito;
import br.com.bali.code.healthcareapitriagem.Triagem.domain.model.StatusDiagnostico;
import br.com.bali.code.healthcareapitriagem.Triagem.domain.model.Triagem;
import br.com.bali.code.healthcareapitriagem.Triagem.domain.repository.DiagnosticoRepository;
import br.com.bali.code.healthcareapitriagem.Triagem.domain.repository.TriagemRepository;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.exception.TriagemNaoEncontradaException;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.dto.TriagemFinalizadaEvent;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.publisher.TriagemEventPublisher;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rest.PacienteClient;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rest.UsuarioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DiagnosticoServiceImp implements DiagnosticoService {

    private final DiagnosticoRepository diagnosticoRepository;
    private final TriagemRepository triagemRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final TriagemEventPublisher eventPublisher;
    private final PacienteClient pacienteClient;
    private final UsuarioClient usuarioClient;

    public DiagnosticoServiceImp(
            DiagnosticoRepository diagnosticoRepository,
            TriagemRepository triagemRepository,
            PdfGeneratorService pdfGeneratorService,
            TriagemEventPublisher eventPublisher,
            PacienteClient pacienteClient,
            UsuarioClient usuarioClient
    ) {
        this.diagnosticoRepository = diagnosticoRepository;
        this.triagemRepository = triagemRepository;
        this.pdfGeneratorService = pdfGeneratorService;
        this.eventPublisher = eventPublisher;
        this.pacienteClient = pacienteClient;
        this.usuarioClient = usuarioClient;
    }

    @Override
    @Transactional
    public DiagnosticoResponse iniciar(Long triagemId, Long medicoId) {
        log.info("[DiagnosticoService] Iniciando diagnóstico para triagemId={} medicoId={}", triagemId, medicoId);

        // 1. Validar que o médico existe e é um médico ativo
        usuarioClient.validarMedico(medicoId);

        // 2. Buscar e validar a Triagem
        Triagem triagem = triagemRepository.findById(triagemId)
                .orElseThrow(() -> new TriagemNaoEncontradaException(triagemId));

        if (triagem.getStatus() == StatusTriagem.FINALIZADO) {
            throw new IllegalStateException("A triagem correspondente já foi finalizada.");
        }

        // 3. Se já existir diagnóstico iniciado, retorna ele
        Optional<Diagnostico> diagExistente = diagnosticoRepository.findByTriagemId(triagemId);
        if (diagExistente.isPresent()) {
            log.info("[DiagnosticoService] Diagnóstico já existe para triagemId={}", triagemId);
            return toResponse(diagExistente.get());
        }

        // 4. Aceitar a triagem: atualiza o médico da triagem e muda o status para EM_ATENDIMENTO
        triagem.setMedicoId(medicoId);
        triagem.setStatus(StatusTriagem.EM_ATENDIMENTO);
        triagemRepository.save(triagem);

        // 5. Criar novo Diagnóstico
        Diagnostico diagnostico = Diagnostico.builder()
                .triagem(triagem)
                .medicoId(medicoId)
                .status(StatusDiagnostico.EM_ANDAMENTO)
                .build();

        Diagnostico salvo = diagnosticoRepository.save(diagnostico);
        return toResponse(salvo);
    }

    @Override
    @Transactional
    public DiagnosticoResponse salvar(Long id, SalvarDiagnosticoRequest request, Long medicoId) {
        log.info("[DiagnosticoService] Salvando rascunho de diagnóstico id={} medicoId={}", id, medicoId);

        Diagnostico diagnostico = diagnosticoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Diagnóstico não encontrado para o ID: " + id));

        if (diagnostico.getStatus() == StatusDiagnostico.ASSINADO) {
            throw new IllegalStateException("Não é possível alterar um diagnóstico já assinado.");
        }

        if (!diagnostico.getMedicoId().equals(medicoId)) {
            throw new IllegalArgumentException("Apenas o médico responsável pelo atendimento pode alterar este diagnóstico.");
        }

        diagnostico.setDescricao(request.descricao());

        // Atualizar lista de medicamentos
        diagnostico.getMedicamentos().clear();
        if (request.medicamentos() != null) {
            for (var medReq : request.medicamentos()) {
                MedicamentoPrescrito med = MedicamentoPrescrito.builder()
                        .diagnostico(diagnostico)
                        .nome(medReq.nome())
                        .dosagem(medReq.dosagem())
                        .frequencia(medReq.frequencia())
                        .prazoUso(medReq.prazoUso())
                        .build();
                diagnostico.getMedicamentos().add(med);
            }
        }

        Diagnostico salvo = diagnosticoRepository.save(diagnostico);
        return toResponse(salvo);
    }

    @Override
    @Transactional
    public DiagnosticoResponse assinar(Long id, Long medicoId, String assinaturaBase64) {
        log.info("[DiagnosticoService] Assinando diagnóstico id={} medicoId={}", id, medicoId);

        Diagnostico diagnostico = diagnosticoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Diagnóstico não encontrado para o ID: " + id));

        if (diagnostico.getStatus() == StatusDiagnostico.ASSINADO) {
            throw new IllegalStateException("O diagnóstico já está assinado.");
        }

        if (!diagnostico.getMedicoId().equals(medicoId)) {
            throw new IllegalArgumentException("Apenas o médico responsável pelo atendimento pode assinar este diagnóstico.");
        }

        if (diagnostico.getDescricao() == null || diagnostico.getDescricao().trim().isEmpty()) {
            throw new IllegalArgumentException("O diagnóstico não pode ser assinado com a descrição em branco.");
        }

        diagnostico.setAssinaturaBase64(assinaturaBase64);

        // 1. Gerar Hash de Assinatura SHA-256
        String hash = calcularHashSHA256(diagnostico);
        diagnostico.setAssinaturaHash(hash);
        diagnostico.setAssinadoEm(LocalDateTime.now());
        diagnostico.setStatus(StatusDiagnostico.ASSINADO);

        // 2. Finalizar a triagem associada
        Triagem triagem = diagnostico.getTriagem();
        triagem.setStatus(StatusTriagem.FINALIZADO);
        triagem.setObservacoes("Atendimento médico concluído. Hash Assinatura: " + hash);
        triagemRepository.save(triagem);

        Diagnostico salvo = diagnosticoRepository.save(diagnostico);

        // 3. Publicar evento TriagemFinalizadaEvent no RabbitMQ
        String pacienteNome = resolverNomePaciente(triagem.getPacienteId());
        eventPublisher.publicarFinalizada(new TriagemFinalizadaEvent(
                triagem.getId(),
                triagem.getPacienteId(),
                pacienteNome,
                triagem.getEnfermeiroId(),
                triagem.getMedicoId(),
                triagem.getStatus().name()
        ));

        return toResponse(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] gerarPdf(Long id) {
        log.info("[DiagnosticoService] Gerando PDF para diagnóstico id={}", id);

        Diagnostico diagnostico = diagnosticoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Diagnóstico não encontrado para o ID: " + id));

        Triagem triagem = diagnostico.getTriagem();

        // Resolver Nomes
        String pacienteNome = resolverNomePaciente(triagem.getPacienteId());
        String pacienteCpf = resolverCpfPaciente(triagem.getPacienteId());
        String enfermeiroNome = resolverNomeUsuario(triagem.getEnfermeiroId());
        String medicoNome = resolverNomeUsuario(diagnostico.getMedicoId());

        return pdfGeneratorService.gerar(
                diagnostico,
                pacienteNome,
                pacienteCpf,
                enfermeiroNome,
                medicoNome
        );
    }

    private String calcularHashSHA256(Diagnostico diagnostico) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("DIAGNOSTICO_ID:").append(diagnostico.getId()).append("|");
            sb.append("TRIAGEM_ID:").append(diagnostico.getTriagem().getId()).append("|");
            sb.append("MEDICO_ID:").append(diagnostico.getMedicoId()).append("|");
            sb.append("DESCRICAO:").append(diagnostico.getDescricao()).append("|");
            if (diagnostico.getMedicamentos() != null) {
                for (MedicamentoPrescrito m : diagnostico.getMedicamentos()) {
                    sb.append(m.getNome()).append("-").append(m.getDosagem()).append(";");
                }
            }
            if (diagnostico.getAssinaturaBase64() != null) {
                sb.append("ASSINATURA:").append(diagnostico.getAssinaturaBase64()).append("|");
            }
            sb.append("TIMESTAMP:").append(System.currentTimeMillis());

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular hash de assinatura", e);
        }
    }

    private String resolverNomePaciente(Long pacienteId) {
        try {
            var pac = pacienteClient.buscarPorId(pacienteId);
            return pac != null ? pac.nome() : "Paciente";
        } catch (Exception e) {
            log.warn("Erro ao obter nome do paciente {}: {}", pacienteId, e.getMessage());
            return "Paciente";
        }
    }

    private String resolverCpfPaciente(Long pacienteId) {
        try {
            var pac = pacienteClient.buscarPorId(pacienteId);
            return pac != null ? pac.cpf() : "N/A";
        } catch (Exception e) {
            log.warn("Erro ao obter CPF do paciente {}: {}", pacienteId, e.getMessage());
            return "N/A";
        }
    }

    private String resolverNomeUsuario(Long usuarioId) {
        if (usuarioId == null) {
            return "N/A";
        }
        try {
            var usr = usuarioClient.buscarPorId(usuarioId);
            return usr != null ? usr.nome() : "N/A";
        } catch (Exception e) {
            log.warn("Erro ao obter nome do usuário {}: {}", usuarioId, e.getMessage());
            return "N/A";
        }
    }

    private DiagnosticoResponse toResponse(Diagnostico d) {
        List<MedicamentoPrescritoResponse> meds = new ArrayList<>();
        if (d.getMedicamentos() != null) {
            meds = d.getMedicamentos().stream()
                    .map(m -> new MedicamentoPrescritoResponse(
                            m.getId(),
                            m.getNome(),
                            m.getDosagem(),
                            m.getFrequencia(),
                            m.getPrazoUso()
                    ))
                    .toList();
        }

        return new DiagnosticoResponse(
                d.getId(),
                d.getTriagem().getId(),
                d.getMedicoId(),
                d.getDescricao(),
                d.getStatus().name(),
                d.getAssinaturaHash(),
                d.getAssinaturaBase64(),
                d.getAssinadoEm(),
                meds,
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
}
