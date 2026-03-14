package br.com.fiap.v2i.processing.video;

import br.com.fiap.v2i.processing.client.V2iWebClient;
import br.com.fiap.v2i.processing.client.UpdateVideoErrorStatusRequest;
import br.com.fiap.v2i.processing.client.UpdateVideoStatusRequest;
import br.com.fiap.v2i.processing.notification.VideoProcessedMessage;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class VideoController {

    private final VideoProcessingService videoProcessingService;
    private final VideoDownloadService videoDownloadService;
    private final V2iWebClient v2iWebClient;
    private final RabbitTemplate rabbitTemplate;

    public VideoController(VideoProcessingService videoProcessingService, VideoDownloadService videoDownloadService, V2iWebClient v2iWebClient, RabbitTemplate rabbitTemplate) {
        this.videoProcessingService = videoProcessingService;
        this.videoDownloadService = videoDownloadService;
        this.v2iWebClient = v2iWebClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping(value = "/extract-frames", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void extractFrames(@RequestBody ExtractFramesRequest request, HttpServletResponse response) {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=frames.zip");
        response.setStatus(HttpServletResponse.SC_OK);

        System.out.println("Key: " + request.key());
        v2iWebClient.markAsProcessing(request.key());

        try (DownloadedVideo downloadedVideo = videoDownloadService.download(request.url(), request.filenameFromUrl())) {
            byte[] zipContent = videoProcessingService.extractFramesAndStream(downloadedVideo.path());
            response.getOutputStream().write(zipContent);
            response.getOutputStream().flush();

            // Gerar URL de download (presigned URL do S3)
            String downloadUrl = generatePresignedDownloadUrl(request.videoId());

            // Atualizar status no v2i-web com URL de download
            v2iWebClient.markAsComplete(new UpdateVideoStatusRequest(request.key(), downloadUrl));

            // Enviar mensagem de sucesso ao RabbitMQ
            VideoProcessedMessage successMessage = new VideoProcessedMessage(
                    request.videoId(),
                    request.userEmail(),
                    "SUCCESS",
                    downloadUrl,
                    null
            );
            rabbitTemplate.convertAndSend("video.processed", successMessage);

        } catch (IOException e) {
            String errorMessage = "Error downloading or processing video: " + e.getMessage();

            // Atualizar status no v2i-web com mensagem de erro
            v2iWebClient.markAsError(new UpdateVideoErrorStatusRequest(request.key(), errorMessage));

            // Enviar mensagem de erro ao RabbitMQ
            VideoProcessedMessage errorMsg = new VideoProcessedMessage(
                    request.videoId(),
                    request.userEmail(),
                    "FAILURE",
                    null,
                    errorMessage
            );
            rabbitTemplate.convertAndSend("video.processed", errorMsg);

            throw new VideoProcessingException(errorMessage, e);
        }
    }

    private String generatePresignedDownloadUrl(String videoId) {
        // TODO: Implementar geração de URL presigned do S3 para o arquivo ZIP
        // Por enquanto, retorna uma URL genérica
        return "/api/video/download/" + videoId;
    }

}
