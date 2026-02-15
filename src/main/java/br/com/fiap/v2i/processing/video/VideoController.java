package br.com.fiap.v2i.processing.video;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class VideoController {

    private final VideoProcessingService videoProcessingService;

    public VideoController(VideoProcessingService videoProcessingService) {
        this.videoProcessingService = videoProcessingService;
    }

    @PostMapping(value = "/extract-frames", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void extractFrames(@RequestParam("file") MultipartFile file, HttpServletResponse response) {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=frames.zip");
        response.setStatus(HttpServletResponse.SC_OK);

        try {
            videoProcessingService.extractFramesAndStream(file, response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("Error processing video", e);
        }
    }
}
