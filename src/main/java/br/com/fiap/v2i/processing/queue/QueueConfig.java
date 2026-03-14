package br.com.fiap.v2i.processing.queue;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueConfig {

    public static final String EXCHANGE_NAME = "video-processing-exchange";
    public static final String QUEUE_NAME = "video-processing-queue";
    public static final String ROUTING_KEY = "video.process";

    @Bean
    Queue processingQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    TopicExchange processingExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    Binding processingBinding(Queue processingQueue, TopicExchange processingExchange) {
        return BindingBuilder.bind(processingQueue).to(processingExchange).with(ROUTING_KEY);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
