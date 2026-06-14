package br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.integrations.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Filas de notificação consumidas pela api-usuarios (eventos publicados pela api-triagem).
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_TRIAGEM = "triagem.exchange";
    public static final String EXCHANGE_DLQ     = "dlq.exchange";

    public static final String QUEUE_TRIAGEM_CRIADA           = "triagem.criada";
    public static final String QUEUE_TRIAGEM_CRIADA_DLQ       = "triagem.criada.dlq";
    public static final String QUEUE_TRIAGEM_CLASSIFICADA     = "triagem.classificada";
    public static final String QUEUE_TRIAGEM_CLASSIFICADA_DLQ = "triagem.classificada.dlq";
    public static final String QUEUE_TRIAGEM_FINALIZADA       = "triagem.finalizada";
    public static final String QUEUE_TRIAGEM_FINALIZADA_DLQ   = "triagem.finalizada.dlq";

    public static final String RK_TRIAGEM_CRIADA           = "triagem.criada";
    public static final String RK_TRIAGEM_CRIADA_DLQ       = "triagem.criada.dlq";
    public static final String RK_TRIAGEM_CLASSIFICADA     = "triagem.classificada";
    public static final String RK_TRIAGEM_CLASSIFICADA_DLQ = "triagem.classificada.dlq";
    public static final String RK_TRIAGEM_FINALIZADA       = "triagem.finalizada";
    public static final String RK_TRIAGEM_FINALIZADA_DLQ   = "triagem.finalizada.dlq";

    @Bean
    public DirectExchange triagemExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_TRIAGEM).durable(true).build();
    }

    @Bean
    public DirectExchange dlqExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_DLQ).durable(true).build();
    }

    @Bean
    public Queue triagemCriadaQueue() {
        return queueWithDlq(QUEUE_TRIAGEM_CRIADA, RK_TRIAGEM_CRIADA_DLQ);
    }

    @Bean
    public Queue triagemCriadaDlq() {
        return QueueBuilder.durable(QUEUE_TRIAGEM_CRIADA_DLQ).build();
    }

    @Bean
    public Binding triagemCriadaBinding(Queue triagemCriadaQueue, DirectExchange triagemExchange) {
        return BindingBuilder.bind(triagemCriadaQueue).to(triagemExchange).with(RK_TRIAGEM_CRIADA);
    }

    @Bean
    public Binding triagemCriadaDlqBinding(Queue triagemCriadaDlq, DirectExchange dlqExchange) {
        return BindingBuilder.bind(triagemCriadaDlq).to(dlqExchange).with(RK_TRIAGEM_CRIADA_DLQ);
    }

    @Bean
    public Queue triagemClassificadaQueue() {
        return queueWithDlq(QUEUE_TRIAGEM_CLASSIFICADA, RK_TRIAGEM_CLASSIFICADA_DLQ);
    }

    @Bean
    public Queue triagemClassificadaDlq() {
        return QueueBuilder.durable(QUEUE_TRIAGEM_CLASSIFICADA_DLQ).build();
    }

    @Bean
    public Binding triagemClassificadaBinding(Queue triagemClassificadaQueue, DirectExchange triagemExchange) {
        return BindingBuilder.bind(triagemClassificadaQueue).to(triagemExchange).with(RK_TRIAGEM_CLASSIFICADA);
    }

    @Bean
    public Binding triagemClassificadaDlqBinding(Queue triagemClassificadaDlq, DirectExchange dlqExchange) {
        return BindingBuilder.bind(triagemClassificadaDlq).to(dlqExchange).with(RK_TRIAGEM_CLASSIFICADA_DLQ);
    }

    @Bean
    public Queue triagemFinalizadaQueue() {
        return queueWithDlq(QUEUE_TRIAGEM_FINALIZADA, RK_TRIAGEM_FINALIZADA_DLQ);
    }

    @Bean
    public Queue triagemFinalizadaDlq() {
        return QueueBuilder.durable(QUEUE_TRIAGEM_FINALIZADA_DLQ).build();
    }

    @Bean
    public Binding triagemFinalizadaBinding(Queue triagemFinalizadaQueue, DirectExchange triagemExchange) {
        return BindingBuilder.bind(triagemFinalizadaQueue).to(triagemExchange).with(RK_TRIAGEM_FINALIZADA);
    }

    @Bean
    public Binding triagemFinalizadaDlqBinding(Queue triagemFinalizadaDlq, DirectExchange dlqExchange) {
        return BindingBuilder.bind(triagemFinalizadaDlq).to(dlqExchange).with(RK_TRIAGEM_FINALIZADA_DLQ);
    }

    @Bean
    public Jackson2JsonMessageConverter jacksonJsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    private static Queue queueWithDlq(String queueName, String dlqRoutingKey) {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLQ)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }
}
