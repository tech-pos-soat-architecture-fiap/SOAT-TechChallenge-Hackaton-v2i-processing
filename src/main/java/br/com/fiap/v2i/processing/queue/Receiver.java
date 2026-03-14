package br.com.fiap.v2i.processing.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class Receiver {

    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);

    private final VideoProcessingJobService jobService;

    public Receiver(VideoProcessingJobService jobService) {
        this.jobService = jobService;
    }

    @RabbitListener(queues = QueueConfig.QUEUE_NAME)
    public void receiveMessage(VideoProcessJobMessage message) {
        logger.info("Received processing job: {}", message);
        jobService.process(message);
    }
}