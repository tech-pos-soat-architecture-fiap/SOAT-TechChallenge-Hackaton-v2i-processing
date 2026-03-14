package br.com.fiap.v2i.processing.client;

public class UpdateVideoStatusRequest {
    private String videoHash;
    private String downloadUrl;

    public UpdateVideoStatusRequest(String videoHash, String downloadUrl) {
        this.videoHash = videoHash;
        this.downloadUrl = downloadUrl;
    }

    public String getVideoHash() {
        return videoHash;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}
