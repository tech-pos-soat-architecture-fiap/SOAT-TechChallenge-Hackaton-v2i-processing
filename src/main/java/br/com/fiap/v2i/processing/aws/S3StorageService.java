package br.com.fiap.v2i.processing.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name:v2i-bucket}")
    private String bucketName;

    public S3StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public Path downloadToTempFile(String key, String filenameHint) throws IOException {
        Path tempDir = Files.createTempDirectory("s3_download_");
        Path out = tempDir.resolve(filenameHint);

        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> in = s3Client.getObject(req)) {
            Files.copy(in, out);
        } catch (S3Exception e) {
            cleanupQuietly(out, tempDir);
            throw e;
        } catch (IOException e) {
            cleanupQuietly(out, tempDir);
            throw e;
        }

        return out;
    }

    public void uploadBytes(String key, byte[] content, String contentType) {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(req, RequestBody.fromBytes(content));
    }

    private void cleanupQuietly(Path file, Path dir) {
        try { if (file != null) Files.deleteIfExists(file); } catch (IOException ignored) {}
        try { if (dir != null) Files.deleteIfExists(dir); } catch (IOException ignored) {}
    }
}

