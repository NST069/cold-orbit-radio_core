package com.coradio.rotation.domain.context;

import com.coradio.rotation.application.dto.response.RadioInfoResponse;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class IcecastStateContextTest {

    private final IcecastStateContext context = new IcecastStateContext();

    @Test
    void shouldConvertToDto() {

        StationInfo station = new StationInfo(
                "Cold Orbit Radio",
                "Interplanetary broadcast station",
                "Various",
                "http://localhost:8000/radio.mp3",
                "Artist - Track",
                5,
                12,
                Instant.parse("2026-07-23T20:27:12Z")
        );

        context.setStationInfo(station);

        RadioInfoResponse dto = context.toDto();

        assertThat(dto.name()).isEqualTo("Cold Orbit Radio");
        assertThat(dto.description()).isEqualTo("Interplanetary broadcast station");
        assertThat(dto.genre()).isEqualTo("Various");
        assertThat(dto.url()).isEqualTo("http://localhost:8000/radio.mp3");
        assertThat(dto.listeners()).isEqualTo(5);
        assertThat(dto.peakListeners()).isEqualTo(12);
        assertThat(dto.currentSong()).isEqualTo("Artist - Track");
        assertThat(dto.streamStart()).isEqualTo("2026-07-23T20:27:12Z");
    }

    @Test
    void shouldReplaceNullValuesWithDefaults() {

        StationInfo station = new StationInfo(
                "Cold Orbit Radio",
                "Description",
                "Various",
                "http://localhost:8000/radio.mp3",
                null,
                null,
                null,
                Instant.parse("2026-07-23T20:27:12Z")
        );

        context.setStationInfo(station);

        RadioInfoResponse dto = context.toDto();

        assertThat(dto.listeners()).isZero();
        assertThat(dto.peakListeners()).isZero();
        assertThat(dto.currentSong()).isEqualTo("No song played");
    }

    @Test
    void shouldReplaceBlankSongWithDefault() {

        StationInfo station = new StationInfo(
                "Cold Orbit Radio",
                "Description",
                "Various",
                "http://localhost:8000/radio.mp3",
                "",
                1,
                2,
                Instant.parse("2026-07-23T20:27:12Z")
        );

        context.setStationInfo(station);

        RadioInfoResponse dto = context.toDto();

        assertThat(dto.currentSong()).isEqualTo("No song played");
    }

    @Test
    void shouldReturnDefaultDtoWhenStationInfoIsMissing() {
        RadioInfoResponse dto = context.toDto();

        assertThat(dto.name()).isEqualTo("Cold Orbit Radio");
        assertThat(dto.listeners()).isZero();
        assertThat(dto.currentSong()).isEqualTo("No song played");
    }
}
