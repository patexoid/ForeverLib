package com.patex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@Slf4j
public class RabbitConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Exchange newBookExchange() {
        return new FanoutExchange("newBookExchange", true, false);
    }
    @Bean
    public Binding marketDataBinding() {
        return BindingBuilder.bind(duplicateQueue()).to(newBookExchange()).with("").noargs();
    }

    @Bean
    public Queue duplicateQueue() {
        return new Queue("duplicateQueue", true);
    }
}
