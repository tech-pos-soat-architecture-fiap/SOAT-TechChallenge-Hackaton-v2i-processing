package br.com.fiap.v2i.processing.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "v2i-web", url = "${v2i.web.url}")
public interface V2iWebClient {

    /**
     * Mark a video as processing in the v2i-web service.
     *
     * @param videoHash the hash of the video to mark as processing
     */
    @PostMapping("/api/video/processing")
    void markAsProcessing(@RequestBody String videoHash);

    /**
     * Mark a video as complete in the v2i-web service.
     *
     * @param request with videoHash and downloadUrl
     */
    @PostMapping("/api/video/complete")
    void markAsComplete(@RequestBody UpdateVideoStatusRequest request);

    /**
     * Mark a video as error in the v2i-web service.
     *
     * @param request with videoHash and errorMessage
     */
    @PostMapping("/api/video/error")
    void markAsError(@RequestBody UpdateVideoErrorStatusRequest request);
}

