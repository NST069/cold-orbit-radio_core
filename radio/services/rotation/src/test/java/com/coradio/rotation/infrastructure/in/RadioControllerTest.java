package com.coradio.rotation.infrastructure.in;

import com.coradio.rotation.application.dto.response.NowPlayingResponse;
import com.coradio.rotation.application.dto.response.PlaybackHistoryItemDto;
import com.coradio.rotation.application.dto.response.RadioInfoResponse;
import com.coradio.rotation.domain.port.in.LastPlayedUseCase;
import com.coradio.rotation.domain.port.in.RadioInfoUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

    @MockitoBean
    private LastPlayedUseCase lastPlayedService;

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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
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

    @Test
    void shouldReturnLastPlayedTracks() throws Exception {

        List<PlaybackHistoryItemDto> response = List.of(
                new PlaybackHistoryItemDto("artist1", "track1", Instant.now().minus(1, ChronoUnit.MINUTES)),
                new PlaybackHistoryItemDto("artist2", "track2", Instant.now().minus(5, ChronoUnit.MINUTES)),
                new PlaybackHistoryItemDto("artist3", "track3", Instant.now().minus(10, ChronoUnit.MINUTES)),
                new PlaybackHistoryItemDto("artist4", "track4", Instant.now().minus(15, ChronoUnit.MINUTES)),
                new PlaybackHistoryItemDto("artist5", "track5", Instant.now().minus(20, ChronoUnit.MINUTES))
        );

        when(lastPlayedService.getLastPlayed()).thenReturn(response);

        mockMvc.perform(get(prefix + "/history"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].artist").value("artist1"))
                .andExpect(jsonPath("$[0].title").value("track1"));

        verify(lastPlayedService).getLastPlayed();
        verifyNoMoreInteractions(lastPlayedService);
    }
}
