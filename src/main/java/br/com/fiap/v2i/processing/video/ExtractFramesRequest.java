package br.com.fiap.v2i.processing.video;

import java.net.URI;
import java.nio.file.Paths;

public record ExtractFramesRequest(String url, String key, String bucket) {

    public String filenameFromUrl() {
        String path = URI.create(url).getPath();
        String filename = Paths.get(path).getFileName().toString();

        if (filename.isBlank()) {
            throw new IllegalArgumentException("Could not extract filename from URL");
        }

        return filename;
    }

    public String videoHashFromUrl() {
        return key;
    }
}
