package br.com.bali.code.healthcareapiusuarios.Usuario.domain.service;

import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.response.NotificacaoResponse;
import br.com.bali.code.healthcareapiusuarios.Usuario.domain.model.Notificacao;
import br.com.bali.code.healthcareapiusuarios.Usuario.domain.repository.NotificacaoRepository;
import br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.exception.NotificacaoNaoEncontradaException;
import br.com.bali.code.healthcareapiusuarios.Usuario.domain.repository.UserRepository;
import br.com.bali.code.healthcareapiusuarios.Usuario.domain.model.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class NotificacaoServiceImp implements NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UserRepository userRepository;

    public NotificacaoServiceImp(NotificacaoRepository notificacaoRepository, UserRepository userRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.userRepository = userRepository;
    }

    private void validarPropriedade(Long usuarioId) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new AccessDeniedException("Usuário não autenticado");
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuarioLogado = userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Usuário não cadastrado"));
        if (!usuarioLogado.getId().equals(usuarioId)) {
            throw new AccessDeniedException("Você não tem permissão para acessar os dados sensíveis de outro usuário.");
        }
    }

    @Override
    public List<NotificacaoResponse> listarPorUsuario(Long usuarioId, Boolean lida) {
        validarPropriedade(usuarioId);
        List<Notificacao> notificacoes = lida == null
                ? notificacaoRepository.findByUsuarioIdOrderByCriadoEmDesc(usuarioId)
                : notificacaoRepository.findByUsuarioIdAndLidaOrderByCriadoEmDesc(usuarioId, lida);

        return notificacoes.stream().map(this::toResponse).toList();
    }

    @Override
    public long contarNaoLidas(Long usuarioId) {
        validarPropriedade(usuarioId);
        return notificacaoRepository.countByUsuarioIdAndLidaFalse(usuarioId);
    }

    @Override
    @Transactional
    public NotificacaoResponse marcarComoLida(Long id, Long usuarioId) {
        validarPropriedade(usuarioId);
        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() -> new NotificacaoNaoEncontradaException(id));

        if (!notificacao.getUsuarioId().equals(usuarioId)) {
            throw new NotificacaoNaoEncontradaException(id);
        }

        notificacao.setLida(true);
        return toResponse(notificacaoRepository.save(notificacao));
    }

    private NotificacaoResponse toResponse(Notificacao n) {
        return new NotificacaoResponse(
                n.getId(),
                n.getUsuarioId(),
                n.getTipo(),
                n.getTitulo(),
                n.getMensagem(),
                n.getTriagemId(),
                n.getLida(),
                n.getCriadoEm()
        );
    }
}
