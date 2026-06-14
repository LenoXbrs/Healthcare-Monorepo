package br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.exception;

public class NotificacaoNaoEncontradaException extends RuntimeException {

    public NotificacaoNaoEncontradaException(Long id) {
        super("Notificação não encontrada: " + id);
    }
}
