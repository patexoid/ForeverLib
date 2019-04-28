package com.patex;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class SpringRabbitMQ {

    private final String duplicateCheckRequest;
    private final String duplicateCheckResponse;

    public SpringRabbitMQ(@Value("${duplicateCheck.requestQueue}") String duplicateCheckRequest,
                          @Value("${duplicateCheck.responseQueue}") String duplicateCheckResponse) {
        this.duplicateCheckRequest = duplicateCheckRequest;
        this.duplicateCheckResponse = duplicateCheckResponse;
    }

    @Bean
    public Queue duplicateCheckRequest() {
        return new Queue(duplicateCheckRequest, false);
    }

    @Bean
    public Queue duplicateCheckResponse() {
        return new Queue(duplicateCheckResponse, false);
    }

    @Bean
    public RabbitAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
