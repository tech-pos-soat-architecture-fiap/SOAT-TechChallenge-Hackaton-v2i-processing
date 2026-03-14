package br.com.fiap.v2i.processing.video;

import br.com.fiap.v2i.processing.client.V2iWebClient;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VideoController.class)
class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VideoDownloadService videoDownloadService;
    @MockitoBean
    private VideoProcessingService videoProcessingService;

    @MockitoBean
    private V2iWebClient v2iWebClient;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void extractFrames_returns_zip_response() throws Exception {
        String body = """
            {
              "url": "http://localhost/dummy.mp4",
              "filenameFromUrl": "dummy.mp4"
            }
            """;

        DownloadedVideo downloadedVideo = new DownloadedVideo(Path.of("tempDir"), Path.of("video.mp4"));
        given(videoDownloadService.download(anyString(), anyString())).willReturn(downloadedVideo);
        given(videoProcessingService.extractFramesAndStream(any())).willReturn(new byte[] { 1, 2, 3 });

        MvcResult result = mockMvc.perform(
                        post("/extract-frames")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/zip"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=frames.zip"))
                .andReturn();

        byte[] responseBytes = result.getResponse().getContentAsByteArray();
        assertThat(responseBytes).isNotNull();
        assertThat(responseBytes).isNotEmpty();
    }

}
