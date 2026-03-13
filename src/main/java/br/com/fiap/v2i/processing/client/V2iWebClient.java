package br.com.fiap.v2i.processing.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "v2i-web", url = "${v2i.web.url}")
public interface V2iWebClient {

    /**
     * Mark a video as processing in the v2i-web service.
     *
     * @param videoHash the hash of the video to mark as processing
     */
    @PostMapping("/api/video/processing/{videoHash}")
    void markAsProcessing(@PathVariable String videoHash);

    /**
     * Mark a video as complete in the v2i-web service.
     *
     * @param videoHash the hash of the video to mark as complete
     */
    @PostMapping("/api/video/complete/{videoHash}")
    void markAsComplete(@PathVariable String videoHash);

    /**
     * Mark a video as error in the v2i-web service.
     *
     * @param videoHash the hash of the video to mark as error
     */
    @PostMapping("/api/video/error/{videoHash}")
    void markAsError(@PathVariable String videoHash);
}

