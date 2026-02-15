package br.com.fiap.v2i.processing.video;

import org.bytedeco.javacv.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class VideoProcessingService {

    public void extractFramesAndStream(MultipartFile videoFile, OutputStream outputStream) throws IOException {
        Path tempDir = Files.createTempDirectory("video_processing_");
        Path tempVideoPath = tempDir.resolve(videoFile.getOriginalFilename());
        videoFile.transferTo(tempVideoPath.toFile());

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(tempVideoPath.toFile());
             ZipOutputStream zos = new ZipOutputStream(outputStream)) {

            grabber.start();

            Java2DFrameConverter converter = new Java2DFrameConverter();

            // Get video length in microseconds
            long lengthInTime = grabber.getLengthInTime();

            // We want 1 frame per second (1,000,000 microseconds)
            long interval = 1_000_000;

            for (long timestamp = 0; timestamp < lengthInTime; timestamp += interval) {
                grabber.setTimestamp(timestamp);

                Frame frame = grabber.grabImage();

                if (frame != null) {
                    BufferedImage bufferedImage = converter.convert(frame);
                    if (bufferedImage != null) {
                        // Create ZIP entry
                        // timestamp / 1,000,000 gives us the second number (e.g., 0s, 1s, 2s)
                        String filename = String.format("sec_%d.jpg", timestamp / 1_000_000);
                        ZipEntry zipEntry = new ZipEntry(filename);
                        zos.putNextEntry(zipEntry);

                        // Write image directly to the ZIP stream
                        ImageIO.write(bufferedImage, "jpg", zos);
                        zos.closeEntry();
                    }
                }
            }

            grabber.stop();
        } finally {
            Files.deleteIfExists(tempVideoPath);
            Files.deleteIfExists(tempDir);
        }
    }
}
