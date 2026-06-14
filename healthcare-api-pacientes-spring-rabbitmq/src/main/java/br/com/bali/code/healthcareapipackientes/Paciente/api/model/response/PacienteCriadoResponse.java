package br.com.bali.code.healthcareapipackientes.Paciente.api.model.response;

/**
 * Resposta retornada internamente quando um paciente é criado via evento.
 * Utilizada para confirmar ao publicador (api-triagem) que o paciente foi persistido.
 */
public record PacienteCriadoResponse(
    Long pacienteId,
    Long triagemId,
    String status
) {}
