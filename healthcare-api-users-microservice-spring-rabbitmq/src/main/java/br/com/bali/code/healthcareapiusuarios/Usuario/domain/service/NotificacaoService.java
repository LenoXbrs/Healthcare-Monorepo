package br.com.bali.code.healthcareapiusuarios.Usuario.domain.service;

import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.response.NotificacaoResponse;

import java.util.List;

public interface NotificacaoService {

    List<NotificacaoResponse> listarPorUsuario(Long usuarioId, Boolean lida);

    long contarNaoLidas(Long usuarioId);

    NotificacaoResponse marcarComoLida(Long id, Long usuarioId);
}
