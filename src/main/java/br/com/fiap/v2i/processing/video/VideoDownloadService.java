package br.com.fiap.v2i.processing.video;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;

@Service
public class VideoDownloadService {

    public DownloadedVideo download(String url, String filename) throws IOException {
        Path tempDir = Files.createTempDirectory("video_download_");
        Path videoPath = tempDir.resolve(filename);

        try (InputStream inputStream = URI.create(url).toURL().openStream()) {
            Files.copy(inputStream, videoPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            deleteQuietly(videoPath);
            deleteQuietly(tempDir);
            throw e;
        }

        return new DownloadedVideo(tempDir, videoPath);
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }

        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

}
