package br.com.fiap.v2i.processing.queue;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.concurrent.TimeUnit;

@Controller
public class QueueController {

    private final RabbitTemplate rabbitTemplate;
    private final Receiver receiver;

    public QueueController(RabbitTemplate rabbitTemplate, Receiver receiver) {
        this.rabbitTemplate = rabbitTemplate;
        this.receiver = receiver;
    }

    @GetMapping("/test-queue")
    public ResponseEntity<?> testQueue() throws InterruptedException {
        System.out.println("Sending message...");
        rabbitTemplate.convertAndSend(QueueConfig.topicExchangeName, "video.process", "Hello from RabbitMQ!");
        receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);

        return ResponseEntity.ok().build();
    }
}
