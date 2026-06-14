package br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * api-triagem CONSOME paciente.criado e PUBLICA eventos em triagem.exchange.
 */
@Configuration
public class RabbitMQConfig {

    // ── Consumo (publicado pela api-pacientes) ───────────────────────────────
    public static final String EXCHANGE_PACIENTE       = "paciente.exchange";
    public static final String QUEUE_PACIENTE_CRIADO   = "paciente.criado";
    public static final String RK_PACIENTE_CRIADO      = "paciente.criado";

    // ── Publicação (consumido pela api-usuarios e outros) ───────────────────
    public static final String EXCHANGE_TRIAGEM        = "triagem.exchange";
    public static final String RK_TRIAGEM_CRIADA       = "triagem.criada";
    public static final String RK_TRIAGEM_CLASSIFICADA = "triagem.classificada";
    public static final String RK_TRIAGEM_FINALIZADA   = "triagem.finalizada";

    @Bean
    public DirectExchange pacienteExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_PACIENTE).durable(true).build();
    }

    @Bean
    public Queue pacienteCriadoQueue() {
        return QueueBuilder.durable(QUEUE_PACIENTE_CRIADO).build();
    }

    @Bean
    public Binding pacienteCriadoBinding(Queue pacienteCriadoQueue, DirectExchange pacienteExchange) {
        return BindingBuilder.bind(pacienteCriadoQueue)
                .to(pacienteExchange)
                .with(RK_PACIENTE_CRIADO);
    }

    @Bean
    public DirectExchange triagemExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_TRIAGEM).durable(true).build();
    }

    @Bean
    public Jackson2JsonMessageConverter jacksonJsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
