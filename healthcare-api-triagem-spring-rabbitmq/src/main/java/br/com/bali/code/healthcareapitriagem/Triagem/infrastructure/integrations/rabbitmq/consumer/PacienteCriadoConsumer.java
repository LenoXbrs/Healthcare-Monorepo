package br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.consumer;

import br.com.bali.code.healthcareapitriagem.Triagem.domain.service.TriagemService;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.config.RabbitMQConfig;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.dto.PacienteCriadoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PacienteCriadoConsumer {

    private final TriagemService triagemService;

    public PacienteCriadoConsumer(TriagemService triagemService) {
        this.triagemService = triagemService;
    }

    /**
     * Quando a recepção cadastra um paciente, a triagem é aberta automaticamente.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_PACIENTE_CRIADO)
    public void consume(PacienteCriadoEvent event) {
        log.info("[paciente.criado] Recebido: pacienteId={} nome={}",
                event.pacienteId(), event.nome());
        triagemService.criarTriagemFromPaciente(event);
    }
}
