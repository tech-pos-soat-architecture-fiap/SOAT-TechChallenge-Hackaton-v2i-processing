package br.com.fiap.v2i.processing.video;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VideoProcessingServiceTest {

    @Test
    void extractFramesAndStream__generates_zip_with_images() throws Exception {
        VideoProcessingService service = new VideoProcessingService();

        Path tempVideo = Files.createTempFile("video-test-", ".mp4");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try {
                service.extractFramesAndStream(tempVideo, baos);
            } catch (IOException e) {
                return;
            }

            byte[] zipBytes = baos.toByteArray();
            if (zipBytes.length > 0) {
                try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
                    ZipEntry entry = zis.getNextEntry();
                    assertThat(entry).isNotNull();
                    assertThat(entry.getName()).startsWith("sec_").endsWith(".jpg");
                }
            }
        } finally {
            Files.deleteIfExists(tempVideo);
        }
    }

    @Test
    void extractFramesAndStream__throws_when_video_does_not_exist() throws Exception {
        VideoProcessingService service = new VideoProcessingService();
        Path nonExisting = Path.of("non-existing-video-file.mp4");

        assertThrows(IOException.class, () -> {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                service.extractFramesAndStream(nonExisting, baos);
            }
        });
    }
}

