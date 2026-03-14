package br.com.fiap.v2i.processing.queue;

import br.com.fiap.v2i.processing.aws.S3StorageService;
import br.com.fiap.v2i.processing.client.UpdateVideoErrorStatusRequest;
import br.com.fiap.v2i.processing.client.UpdateVideoStatusRequest;
import br.com.fiap.v2i.processing.client.V2iWebClient;
import br.com.fiap.v2i.processing.notification.VideoProcessedMessage;
import br.com.fiap.v2i.processing.video.VideoProcessingException;
import br.com.fiap.v2i.processing.video.VideoProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class VideoProcessingJobService {

    private static final Logger logger = LoggerFactory.getLogger(VideoProcessingJobService.class);

    private final S3StorageService s3StorageService;
    private final VideoProcessingService videoProcessingService;
    private final V2iWebClient v2iWebClient;
    private final RabbitTemplate rabbitTemplate;

    public VideoProcessingJobService(S3StorageService s3StorageService,
                                    VideoProcessingService videoProcessingService,
                                    V2iWebClient v2iWebClient,
                                    RabbitTemplate rabbitTemplate) {
        this.s3StorageService = s3StorageService;
        this.videoProcessingService = videoProcessingService;
        this.v2iWebClient = v2iWebClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void process(VideoProcessJobMessage job) {
        if (job == null || job.getVideoHash() == null || job.getVideoHash().isBlank()) {
            logger.warn("Received invalid job: {}", job);
            return;
        }

        String videoHash = job.getVideoHash();
        logger.info("Processing job: {}", job);

        v2iWebClient.markAsProcessing(videoHash);

        try {
            // 1) Baixa o vídeo do S3 usando a key original (videoHash)
            String filenameHint = filenameFromKey(videoHash);
            Path videoPath = s3StorageService.downloadToTempFile(videoHash, filenameHint);

            // 2) Processa e gera o zip (em memória)
            byte[] zipBytes = videoProcessingService.extractFramesAndStream(videoPath);

            // 3) Faz upload do zip no S3
            String zipKey = outputZipKey(videoHash);
            s3StorageService.uploadBytes(zipKey, zipBytes, "application/zip");

            // 4) Atualiza o v2i-web com um link de download (v2i-web pode gerar presigned GET do zip)
            String downloadUrl = "/api/video/download/" + job.getVideoId();
            v2iWebClient.markAsComplete(new UpdateVideoStatusRequest(videoHash, downloadUrl));

            // 5) Notifica usuário
            VideoProcessedMessage successMsg = new VideoProcessedMessage(job.getVideoId(), job.getUserEmail(), "SUCCESS", downloadUrl, null);
            rabbitTemplate.convertAndSend("video-notification-exchange", "video.processed", successMsg);

        } catch (Exception e) {
            String err = "Error processing video: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            logger.error(err, e);

            v2iWebClient.markAsError(new UpdateVideoErrorStatusRequest(videoHash, err));

            VideoProcessedMessage errorMsg = new VideoProcessedMessage(job.getVideoId(), job.getUserEmail(), "FAILURE", null, err);
            rabbitTemplate.convertAndSend("video-notification-exchange", "video.processed", errorMsg);

            throw new VideoProcessingException(err, e);
        }
    }

    private String filenameFromKey(String key) {
        int idx = key.lastIndexOf('/');
        return idx >= 0 ? key.substring(idx + 1) : key;
    }

    private String outputZipKey(String originalVideoKey) {
        // Ex: uploads/<uuid>/<file>.mp4 -> outputs/<uuid>/frames.zip
        // Se não tiver esse padrão, ainda assim gera algo determinístico
        String[] parts = originalVideoKey.split("/");
        if (parts.length >= 2) {
            return "outputs/" + parts[1] + "/frames.zip";
        }
        return "outputs/" + originalVideoKey.replace('/', '_') + "/frames.zip";
    }
}
