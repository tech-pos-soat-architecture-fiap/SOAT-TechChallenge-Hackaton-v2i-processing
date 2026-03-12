package br.com.fiap.v2i.processing.video;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class VideoController {

    private final VideoProcessingService videoProcessingService;
    private final VideoDownloadService videoDownloadService;

    public VideoController(VideoProcessingService videoProcessingService, VideoDownloadService videoDownloadService) {
        this.videoProcessingService = videoProcessingService;
        this.videoDownloadService = videoDownloadService;
    }

    @PostMapping(value = "/extract-frames", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void extractFrames(@RequestBody ExtractFramesRequest request, HttpServletResponse response) {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=frames.zip");
        response.setStatus(HttpServletResponse.SC_OK);

        try (DownloadedVideo downloadedVideo = videoDownloadService.download(request.url(), request.filenameFromUrl())) {
            videoProcessingService.extractFramesAndStream(downloadedVideo.path(), response.getOutputStream());
        } catch (IOException e) {
            throw new VideoProcessingException("Error downloading or processing video from URL", e);
        }

    }

}
