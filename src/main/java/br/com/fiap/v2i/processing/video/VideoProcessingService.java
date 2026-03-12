package br.com.fiap.v2i.processing.video;

import org.bytedeco.javacv.*;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class VideoProcessingService {

    public void extractFramesAndStream(Path videoPath, OutputStream outputStream) throws IOException {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath.toFile());
             ZipOutputStream zos = new ZipOutputStream(outputStream)) {

            grabber.start();

            Java2DFrameConverter converter = new Java2DFrameConverter();

            long lengthInTime = grabber.getLengthInTime();
            long interval = 1_000_000;

            for (long timestamp = 0; timestamp < lengthInTime; timestamp += interval) {
                grabber.setTimestamp(timestamp);

                Frame frame = grabber.grabImage();

                if (frame != null) {
                    BufferedImage bufferedImage = converter.convert(frame);
                    if (bufferedImage != null) {
                        String filename = String.format("sec_%d.jpg", timestamp / 1_000_000);
                        ZipEntry zipEntry = new ZipEntry(filename);
                        zos.putNextEntry(zipEntry);
                        ImageIO.write(bufferedImage, "jpg", zos);
                        zos.closeEntry();
                    }
                }
            }

            grabber.stop();
        }
    }

}
