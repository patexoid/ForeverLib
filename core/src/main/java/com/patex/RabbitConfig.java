package com.patex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SerializerMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@Slf4j
public class RabbitConfig {
    public static final String BOOK_DUPLICATE_EXCHANGE = "bookDuplicateExchange";

    public static final String BOOK_COVER_EXCHANGE = "bookCoverExchange";


    public static final String DUPLICATE_QUEUE = "duplicateQueue";

    public static final String COVER_QUEUE = "coverQueue";
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new SerializerMessageConverter();
    }

    @Bean
    public Exchange bookDuplicateExchange() {
        return new FanoutExchange(BOOK_DUPLICATE_EXCHANGE, true, false);
    }


    @Bean
    public Exchange bookCoverExchange() {
        return new FanoutExchange(BOOK_COVER_EXCHANGE, true, false);
    }
    @Bean
    public Binding duplicateBinding() {
        return BindingBuilder.bind(duplicateQueue()).to(bookDuplicateExchange()).with("").noargs();
    }

    @Bean
    public Binding coverBinding() {
        return BindingBuilder.bind(coverQueue()).to(bookCoverExchange()).with("").noargs();
    }

    @Bean
    public Queue duplicateQueue() {
        return new Queue(DUPLICATE_QUEUE, true);
    }

    @Bean
    public Queue coverQueue() {
        return new Queue(COVER_QUEUE, true);
    }
}
