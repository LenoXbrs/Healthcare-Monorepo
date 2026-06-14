package br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.publisher;

import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.config.RabbitMQConfig;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.dto.TriagemClassificadaEvent;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.dto.TriagemCriadaEvent;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.dto.TriagemFinalizadaEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TriagemEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public TriagemEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarCriada(TriagemCriadaEvent payload) {
        log.info("[triagem.criada] Publicando: triagemId={} pacienteId={}",
                payload.triagemId(), payload.pacienteId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_TRIAGEM,
                RabbitMQConfig.RK_TRIAGEM_CRIADA,
                payload
        );
    }

    public void publicarClassificada(TriagemClassificadaEvent payload) {
        log.info("[triagem.classificada] Publicando: medicoId={} prioridade={}",
                payload.medicoId(), payload.prioridade());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_TRIAGEM,
                RabbitMQConfig.RK_TRIAGEM_CLASSIFICADA,
                payload
        );
    }

    public void publicarFinalizada(TriagemFinalizadaEvent payload) {
        log.info("[triagem.finalizada] Publicando: triagemId={}", payload.triagemId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_TRIAGEM,
                RabbitMQConfig.RK_TRIAGEM_FINALIZADA,
                payload
        );
    }
}
