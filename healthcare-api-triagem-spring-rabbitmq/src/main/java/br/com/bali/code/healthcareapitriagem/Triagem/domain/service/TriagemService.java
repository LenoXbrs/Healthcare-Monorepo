package br.com.bali.code.healthcareapitriagem.Triagem.domain.service;

import br.com.bali.code.healthcareapitriagem.Triagem.api.model.request.AbrirTriagemRequest;
import br.com.bali.code.healthcareapitriagem.Triagem.api.model.request.ClassificarTriagemRequest;
import br.com.bali.code.healthcareapitriagem.Triagem.api.model.request.FinalizarTriagemRequest;
import br.com.bali.code.healthcareapitriagem.Triagem.api.model.response.*;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.dto.PacienteCriadoEvent;

import java.util.List;

public interface TriagemService {

    /**
     * Chamado pelo consumer de paciente.criado — cria triagem automaticamente.
     */
    void criarTriagemFromPaciente(PacienteCriadoEvent event);

    /**
     * Enfermeiro registra queixa e assume a triagem já criada pelo evento.
     */
    TriagemAbertaResponse abrir(AbrirTriagemRequest request);

    TriagemClassificadaResponse classificar(Long id, ClassificarTriagemRequest request);

    List<TriagemListItemResponse> listar();

    TriagemDetalheResponse buscarPorId(Long id);

    TriagemFinalizadaResponse finalizar(Long id, FinalizarTriagemRequest request);
    TriagemListItemResponse checkIn(Long pacienteId);
    TriagemDetalheResponse chamar(Long id, String sala);
}
