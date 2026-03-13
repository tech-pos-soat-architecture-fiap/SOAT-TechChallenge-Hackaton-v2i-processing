package br.com.fiap.v2i.processing.video;

import br.com.fiap.v2i.processing.client.V2iWebClient;
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

        v2iWebClient.markAsProcessing(request.videoHashFromUrl());

        try (DownloadedVideo downloadedVideo = videoDownloadService.download(request.url(), request.filenameFromUrl())) {
            videoProcessingService.extractFramesAndStream(downloadedVideo.path(), response.getOutputStream());
            rabbitTemplate.convertAndSend("video.processed", request.videoHashFromUrl());
            v2iWebClient.markAsComplete(request.videoHashFromUrl());
        } catch (IOException e) {
            v2iWebClient.markAsError(request.videoHashFromUrl());
            throw new VideoProcessingException("Error downloading or processing video from URL", e);
        }

    }

}
