package br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.integrations.rabbitmq.consumer;

import br.com.bali.code.healthcareapiusuarios.Usuario.domain.model.EventoTriagemLog;
import br.com.bali.code.healthcareapiusuarios.Usuario.domain.repository.EventoTriagemLogRepository;
import br.com.bali.code.healthcareapiusuarios.Usuario.domain.service.NotificationService;
import br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.integrations.rabbitmq.config.RabbitMQConfig;
import br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.integrations.rabbitmq.dto.TriagemClassificadaPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class TriagemClassificadaConsumer {

    private final NotificationService notificationService;
    private final EventoTriagemLogRepository eventLogRepository;
    private final ObjectMapper objectMapper;

    public TriagemClassificadaConsumer(NotificationService notificationService,
                                       EventoTriagemLogRepository eventLogRepository,
                                       ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.eventLogRepository = eventLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Consome eventos triagem.classificada.
     * Sem try/catch genérico: exceções propagam para DLQ via x-dead-letter-exchange.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_TRIAGEM_CLASSIFICADA)
    public void consume(TriagemClassificadaPayload payload) {
        log.info("[triagem.classificada] Recebida: medicoId={} prioridade={}",
                payload.medicoId(), payload.prioridade());

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            eventLogRepository.save(EventoTriagemLog.builder()
                    .tipoEvento("TRIAGEM_CLASSIFICADA")
                    .triagemId(payload.triagemId())
                    .payload(jsonPayload)
                    .recebidoEm(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Erro ao registrar log do evento TRIAGEM_CLASSIFICADA: {}", e.getMessage());
        }

        notificationService.notificarMedicoTriagemClassificada(payload);

        log.info("[triagem.classificada] Notificação enfileirada para medicoId={}", payload.medicoId());
    }
}
