package com.coradio.rotation.domain.context;

import com.coradio.rotation.application.dto.TrackInfo;
import com.coradio.rotation.application.dto.response.NowPlayingResponse;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NowPlayingStateContextTest {

    private final NowPlayingStateContext context = new NowPlayingStateContext();

    @Test
    void shouldConvertCurrentTrackToDto() {

        UUID id = UUID.randomUUID();

        TrackInfo track = new TrackInfo(
                id,
                "Artist",
                "Track",
                245,
                null
        );

        context.setCurrentTrack(track);

        NowPlayingResponse dto = context.toDto();

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.performer()).isEqualTo("Artist");
        assertThat(dto.title()).isEqualTo("Track");
        assertThat(dto.duration()).isEqualTo(245);
        assertThat(dto.hasCover()).isFalse();
    }

    @Test
    void shouldReturnEmptyDtoWhenNothingPlaying() {

        NowPlayingResponse dto = context.toDto();

        assertThat(dto.id()).isNull();
        assertThat(dto.performer()).isNull();
        assertThat(dto.title()).isNull();
        assertThat(dto.duration()).isZero();
        assertThat(dto.hasCover()).isFalse();
    }
}
