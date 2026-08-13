package com.coradio.rotation.infrastructure.in;

import com.coradio.rotation.application.dto.response.NowPlayingResponse;
import com.coradio.rotation.application.dto.response.RadioInfoResponse;
import com.coradio.rotation.domain.port.in.RadioInfoUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RadioController.class)
class RadioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${api.prefix:/api/v1}")
    private String prefix;

    @MockitoBean
    private RadioInfoUseCase radioInfoService;

    @Test
    void shouldReturnNowPlaying() throws Exception {

        NowPlayingResponse response = new NowPlayingResponse(
                UUID.randomUUID(),
                "ATLAS",
                "KTRSS",
                212,
                true
        );

        when(radioInfoService.getNowPlaying()).thenReturn(response);

        mockMvc.perform(get(prefix + "/now-playing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("ATLAS"))
                .andExpect(jsonPath("$.performer").value("KTRSS"))
                .andExpect(jsonPath("$.duration").value(212))
                .andExpect(jsonPath("$.hasCover").value(true));

        verify(radioInfoService).getNowPlaying();
        verifyNoMoreInteractions(radioInfoService);
    }

    @Test
    void shouldReturnRadioInfo() throws Exception {

        RadioInfoResponse response = new RadioInfoResponse(
                "Cold Orbit Radio",
                "Interplanetary broadcast station",
                "Various",
                "http://localhost:8000/radio.mp3",
                3,
                12,
                "KTRSS - ATLAS",
                "2026-07-23T20:27:12Z"
        );

        when(radioInfoService.getRadioInfo()).thenReturn(response);

        mockMvc.perform(get(prefix + "/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cold Orbit Radio"))
                .andExpect(jsonPath("$.description").value("Interplanetary broadcast station"))
                .andExpect(jsonPath("$.genre").value("Various"))
                .andExpect(jsonPath("$.url").value("http://localhost:8000/radio.mp3"))
                .andExpect(jsonPath("$.currentSong").value("KTRSS - ATLAS"))
                .andExpect(jsonPath("$.listeners").value(3))
                .andExpect(jsonPath("$.peakListeners").value(12));

        verify(radioInfoService).getRadioInfo();
        verifyNoMoreInteractions(radioInfoService);
    }
}
