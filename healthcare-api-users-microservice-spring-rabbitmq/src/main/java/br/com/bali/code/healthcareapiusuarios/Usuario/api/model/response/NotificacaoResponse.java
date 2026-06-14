package br.com.bali.code.healthcareapiusuarios.Usuario.api.model.response;

import br.com.bali.code.healthcareapiusuarios.Usuario.application.TipoNotificacao;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificacaoResponse(
        Long id,
        Long usuarioId,
        TipoNotificacao tipo,
        String titulo,
        String mensagem,
        Long triagemId,
        Boolean lida,
        LocalDateTime criadoEm
) {}
