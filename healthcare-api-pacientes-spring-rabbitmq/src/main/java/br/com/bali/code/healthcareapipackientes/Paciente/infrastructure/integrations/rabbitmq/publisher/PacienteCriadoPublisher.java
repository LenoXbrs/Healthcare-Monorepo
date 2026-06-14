package br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.integrations.rabbitmq.publisher;

import br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.integrations.rabbitmq.config.RabbitMQConfig;
import br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.integrations.rabbitmq.dto.PacienteCriadoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PacienteCriadoPublisher {

    private final RabbitTemplate rabbitTemplate;

    public PacienteCriadoPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica o evento paciente.criado na exchange paciente.exchange.
     * A api-triagem consome este evento para atualizar o estado da triagem
     * com o pacienteId gerado por este serviço.
     */
    public void publicar(PacienteCriadoEvent payload) {
        log.info("[paciente.criado] Publicando: pacienteId={} pacienteId={}",
                payload.pacienteId(), payload.pacienteId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_PACIENTE,
                RabbitMQConfig.RK_PACIENTE_CRIADO,
                payload
        );
    }
}
