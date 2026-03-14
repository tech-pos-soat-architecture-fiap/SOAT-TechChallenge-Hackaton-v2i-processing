package br.com.fiap.v2i.processing.client;

public class UpdateVideoErrorStatusRequest {
    private String videoHash;
    private String errorMessage;

    public UpdateVideoErrorStatusRequest(String videoHash, String errorMessage) {
        this.videoHash = videoHash;
        this.errorMessage = errorMessage;
    }

    public String getVideoHash() {
        return videoHash;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
