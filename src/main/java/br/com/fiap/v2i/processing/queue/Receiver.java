package br.com.fiap.v2i.processing.queue;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class Receiver {

    @RabbitListener(queues = QueueConfig.queueName)
    public void receiveMessage(String message) {
        System.out.println("Received <" + message + ">");
    }
}