package br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.exception;

public class PacienteNaoEncontradoException extends RuntimeException {

    public PacienteNaoEncontradoException(Long id) {
        super("Paciente não encontrado: id=" + id);
    }

    public PacienteNaoEncontradoException(String info) {
        super("Paciente não encontrado: " + info);
    }
}
