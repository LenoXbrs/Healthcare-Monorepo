package br.com.bali.code.healthcareapipackientes.Paciente.infrastructure.integrations.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_PACIENTE_CRIADO = "paciente.criado";
    public static final String EXCHANGE_PACIENTE = "paciente.exchange";
    public static final String RK_PACIENTE_CRIADO = "paciente.criado";



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
    public Jackson2JsonMessageConverter jacksonJsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
