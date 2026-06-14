package br.com.bali.code.healthcareapipackientes.Paciente.domain.service;

import br.com.bali.code.healthcareapipackientes.Paciente.api.model.request.AtualizarPacienteRequest;
import br.com.bali.code.healthcareapipackientes.Paciente.api.model.request.CriarPacienteRequest;
import br.com.bali.code.healthcareapipackientes.Paciente.api.model.response.PacienteResponse;
import br.com.bali.code.healthcareapipackientes.Paciente.application.StatusPaciente;

import java.util.List;

public interface PacienteService {


    PacienteResponse criar(CriarPacienteRequest request);

    PacienteResponse buscarPorId(Long id);

    PacienteResponse buscarPorCpf(String cpf);

    List<PacienteResponse> listarPorStatus(StatusPaciente status);

    PacienteResponse atualizar(Long id, AtualizarPacienteRequest request);


}
