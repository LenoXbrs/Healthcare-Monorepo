package br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.integrations.rabbitmq.consumer;

import br.com.bali.code.healthcareapiusuarios.Usuario.domain.model.EventoTriagemLog;
import br.com.bali.code.healthcareapiusuarios.Usuario.domain.repository.EventoTriagemLogRepository;
import br.com.bali.code.healthcareapiusuarios.Usuario.domain.service.NotificationService;
import br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.integrations.rabbitmq.config.RabbitMQConfig;
import br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.integrations.rabbitmq.dto.TriagemFinalizadaPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class TriagemFinalizadaConsumer {

    private final NotificationService notificationService;
    private final EventoTriagemLogRepository eventLogRepository;
    private final ObjectMapper objectMapper;

    public TriagemFinalizadaConsumer(NotificationService notificationService,
                                     EventoTriagemLogRepository eventLogRepository,
                                     ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.eventLogRepository = eventLogRepository;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TRIAGEM_FINALIZADA)
    public void consume(TriagemFinalizadaPayload payload) {
        log.info("[triagem.finalizada] Recebida: triagemId={} paciente={}",
                payload.triagemId(), payload.pacienteNome());

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            eventLogRepository.save(EventoTriagemLog.builder()
                    .tipoEvento("TRIAGEM_FINALIZADA")
                    .triagemId(payload.triagemId())
                    .payload(jsonPayload)
                    .recebidoEm(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Erro ao registrar log do evento TRIAGEM_FINALIZADA: {}", e.getMessage());
        }

        notificationService.notificarEquipeTriagemFinalizada(payload);
    }
}
