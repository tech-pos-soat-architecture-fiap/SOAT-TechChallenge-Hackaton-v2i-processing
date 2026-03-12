package br.com.fiap.v2i.processing.video;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VideoDownloadServiceTest {

    @Test
    void download__should_save_file_and_return_downloaded_video() throws Exception {
        byte[] videoBytes = "fake-video-content".getBytes();

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/video", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                exchange.sendResponseHeaders(200, videoBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(videoBytes);
                }
            }
        });
        server.start();

        String url = "http://localhost:" + server.getAddress().getPort() + "/video";
        VideoDownloadService service = new VideoDownloadService();

        DownloadedVideo downloadedVideo = null;
        try {
            downloadedVideo = service.download(url, "test-video.mp4");

            Path tempDir = downloadedVideo.tempDir();
            Path videoPath = downloadedVideo.path();

            assertThat(Files.exists(tempDir)).isTrue();
            assertThat(Files.exists(videoPath)).isTrue();

            byte[] storedBytes = Files.readAllBytes(videoPath);
            assertThat(storedBytes).isEqualTo(videoBytes);
        } finally {
            if (downloadedVideo != null) {
                downloadedVideo.close();
            }
            server.stop(0);
        }

        assertThat(Files.exists(downloadedVideo.tempDir())).isFalse();
        assertThat(Files.exists(downloadedVideo.path())).isFalse();
    }

    @Test
    void download__should_propagate_exception_for_invalid_url() {
        VideoDownloadService service = new VideoDownloadService();
        String invalidUrl = "http://localhost:1/not-available";

        assertThrows(Exception.class, () -> service.download(invalidUrl, "video.mp4"));
    }
}

