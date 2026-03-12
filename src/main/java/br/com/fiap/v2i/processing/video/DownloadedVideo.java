package br.com.fiap.v2i.processing.video;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record DownloadedVideo(Path tempDir, Path path) implements AutoCloseable {

    @Override
    public void close() {
        deleteQuietly(path);
        deleteQuietly(tempDir);
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
