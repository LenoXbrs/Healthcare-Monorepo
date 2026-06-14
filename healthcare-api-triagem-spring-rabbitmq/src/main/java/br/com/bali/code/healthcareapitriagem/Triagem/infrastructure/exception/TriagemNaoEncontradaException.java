package br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.exception;

public class TriagemNaoEncontradaException extends RuntimeException {

    public TriagemNaoEncontradaException(Long id) {
        super("Triagem não encontrada: " + id);
    }

    public TriagemNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
