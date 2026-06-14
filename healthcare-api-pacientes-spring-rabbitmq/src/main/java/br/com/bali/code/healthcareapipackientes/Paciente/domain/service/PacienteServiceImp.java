package br.com.bali.code.healthcareapipackientes.Paciente.domain.service;

import br.com.bali.code.healthcareapipackientes.Paciente.api.model.request.AtualizarPacienteRequest;
import br.com.bali.code.healthcareapipackientes.Paciente.api.model.request.CriarPacienteRequest;
import br.com.bali.code.healthcareapipackientes.Paciente.api.model.response.PacienteResponse;
import br.com.bali.code.healthcareapipackientes.Paciente.application.StatusPaciente;
import br.com.bali.code.healthcareapipackientes.Paciente.domain.model.Paciente;
import br.com.bali.code.healthcareapipackientes.Paciente.domain.repository.PacienteRepository;
import br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.exception.PacienteNaoEncontradoException;
import br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.integrations.rabbitmq.dto.PacienteCriadoEvent;
import br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.integrations.rabbitmq.publisher.PacienteCriadoPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class PacienteServiceImp implements PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PacienteCriadoPublisher publisher;


    public PacienteServiceImp(PacienteRepository pacienteRepository,
                              PacienteCriadoPublisher publisher, RabbitTemplate rabbitTemplate) {
        this.pacienteRepository = pacienteRepository;
        this.publisher = publisher;
    }


    @Transactional
    public PacienteResponse criar(CriarPacienteRequest request) {
        Paciente paciente = Paciente.builder()
                .nome(request.nome())
                .cpf(request.cpf())
                .status(StatusPaciente.AGUARDANDO_TRIAGEM)
                .ativo(true)
                .build();

        Paciente salvo = pacienteRepository.save(paciente);

        publisher.publicar(
                new PacienteCriadoEvent(
                        salvo.getId(),
                        salvo.getNome(),
                        salvo.getCpf(),
                        salvo.getStatus()
                )
        );

        return toResponse(salvo);
    }
    @Override
    @Cacheable(value = "pacientes", key = "#id")
    public PacienteResponse buscarPorId(Long id) {
        return pacienteRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new PacienteNaoEncontradoException(id));
    }

    @Override
    public PacienteResponse buscarPorCpf(String cpf) {
        return pacienteRepository.findByCpf(cpf)
                .map(this::toResponse)
                .orElseThrow(() -> new PacienteNaoEncontradoException("CPF: " + cpf));
    }

    @Override
    @Cacheable(value = "pacientes-status", key = "#status")
    public List<PacienteResponse> listarPorStatus(StatusPaciente status) {
        return pacienteRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "pacientes", key = "#id"),
        @CacheEvict(value = "pacientes-status", allEntries = true)
    })
    public PacienteResponse atualizar(Long id, AtualizarPacienteRequest request) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new PacienteNaoEncontradoException(id));

        paciente.setNome(request.nome());
        paciente.setStatus(request.status());
        paciente.setAtivo(request.ativo());

        return toResponse(pacienteRepository.save(paciente));
    }

    private PacienteResponse toResponse(Paciente p) {
        return new PacienteResponse(
                p.getId(),
                p.getNome(),
                p.getCpf(),
                p.getStatus(),
                p.getAtivo(),
                p.getCreatedAt()
        );
    }
}
