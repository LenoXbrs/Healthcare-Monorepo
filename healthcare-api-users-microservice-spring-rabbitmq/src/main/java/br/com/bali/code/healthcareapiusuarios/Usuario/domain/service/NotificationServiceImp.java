package br.com.bali.code.healthcareapiusuarios.Usuario.domain.service;

import br.com.bali.code.healthcareapiusuarios.Usuario.application.Role;
import br.com.bali.code.healthcareapiusuarios.Usuario.application.TipoNotificacao;
import br.com.bali.code.healthcareapiusuarios.Usuario.domain.model.Notificacao;
import br.com.bali.code.healthcareapiusuarios.Usuario.domain.model.User;
import br.com.bali.code.healthcareapiusuarios.Usuario.domain.repository.NotificacaoRepository;
import br.com.bali.code.healthcareapiusuarios.Usuario.domain.repository.UserRepository;
import br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.integrations.rabbitmq.dto.TriagemClassificadaPayload;
import br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.integrations.rabbitmq.dto.TriagemCriadaPayload;
import br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.integrations.rabbitmq.dto.TriagemFinalizadaPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class NotificationServiceImp implements NotificationService {

    private final UserRepository userRepository;
    private final NotificacaoRepository notificacaoRepository;

    public NotificationServiceImp(UserRepository userRepository,
                                  NotificacaoRepository notificacaoRepository) {
        this.userRepository = userRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    @Override
    @Async
    @Transactional
    public void notificarEnfermeirosTriagemCriada(TriagemCriadaPayload payload) {
        List<User> enfermeiros = userRepository.findByRoleAndActiveTrue(Role.ENFERMEIRO);
        if (enfermeiros.isEmpty()) {
            log.warn("[notificação] Nenhum enfermeiro ativo para triagemId={}", payload.triagemId());
            return;
        }

        String titulo = "Novo paciente na fila de triagem";
        String mensagem = String.format(
                "Paciente %s aguarda triagem (triagem #%d).",
                payload.pacienteNome(),
                payload.triagemId()
        );

        for (User enfermeiro : enfermeiros) {
            salvarNotificacao(enfermeiro.getId(), TipoNotificacao.TRIAGEM_CRIADA, titulo, mensagem, payload.triagemId());
            enviarCanalExterno(enfermeiro, titulo, mensagem);
        }

        log.info("[notificação] {} enfermeiro(s) notificado(s) — triagemId={}",
                enfermeiros.size(), payload.triagemId());
    }

    @Override
    @Async
    @Transactional
    public void notificarMedicoTriagemClassificada(TriagemClassificadaPayload payload) {
        User medico = userRepository.findById(payload.medicoId()).orElse(null);
        String nomeMedico = medico != null ? medico.getName() : "médico #" + payload.medicoId();

        String titulo = "Paciente classificado — prioridade " + payload.prioridade();
        String mensagem = String.format(
                "Dr(a). %s, o paciente %s foi classificado com prioridade %s. Dirija-se à sala de triagem.",
                nomeMedico,
                payload.pacienteNome(),
                payload.prioridade()
        );

        salvarNotificacao(payload.medicoId(), TipoNotificacao.TRIAGEM_CLASSIFICADA, titulo, mensagem, payload.triagemId());

        if (medico != null) {
            enviarCanalExterno(medico, titulo, mensagem);
        }

        log.info("[notificação] Médico notificado: medicoId={} triagemId={}",
                payload.medicoId(), payload.triagemId());
    }

    @Override
    @Async
    @Transactional
    public void notificarEquipeTriagemFinalizada(TriagemFinalizadaPayload payload) {
        String titulo = "Triagem finalizada";
        String mensagem = String.format(
                "Triagem #%d do paciente %s foi finalizada.",
                payload.triagemId(),
                payload.pacienteNome()
        );

        if (payload.enfermeiroId() != null) {
            userRepository.findById(payload.enfermeiroId()).ifPresent(enfermeiro -> {
                salvarNotificacao(enfermeiro.getId(), TipoNotificacao.TRIAGEM_FINALIZADA, titulo, mensagem, payload.triagemId());
                enviarCanalExterno(enfermeiro, titulo, mensagem);
            });
        }

        if (payload.medicoId() != null) {
            userRepository.findById(payload.medicoId()).ifPresent(medico -> {
                salvarNotificacao(medico.getId(), TipoNotificacao.TRIAGEM_FINALIZADA, titulo, mensagem, payload.triagemId());
                enviarCanalExterno(medico, titulo, mensagem);
            });
        }

        log.info("[notificação] Equipe notificada — triagemId={}", payload.triagemId());
    }

    private void salvarNotificacao(Long usuarioId, TipoNotificacao tipo, String titulo, String mensagem, Long triagemId) {
        notificacaoRepository.save(Notificacao.builder()
                .usuarioId(usuarioId)
                .tipo(tipo)
                .titulo(titulo)
                .mensagem(mensagem)
                .triagemId(triagemId)
                .lida(false)
                .build());
    }

    private void enviarCanalExterno(User usuario, String titulo, String mensagem) {
        if (usuario.getEmail() != null) {
            log.info("[e-mail simulado] Para: {} | Assunto: {} | {}", usuario.getEmail(), titulo, mensagem);
        }
    }
}
