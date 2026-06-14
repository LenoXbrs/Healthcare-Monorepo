package br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.exception;

public class PacienteNaoEncontradoException extends RuntimeException {

    public PacienteNaoEncontradoException(Long id) {
        super("Paciente não encontrado: " + id);
    }
}
