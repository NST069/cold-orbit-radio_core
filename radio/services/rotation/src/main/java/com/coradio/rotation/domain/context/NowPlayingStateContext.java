package com.coradio.rotation.domain.context;

import com.coradio.rotation.application.dto.TrackInfo;
import com.coradio.rotation.application.dto.response.NowPlayingResponse;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Getter
public class NowPlayingStateContext {

    private TrackInfo currentTrack;

    private Instant startedAt;

    public void set(TrackInfo trackInfo) {
        this.currentTrack = trackInfo;
        this.startedAt = Instant.now();
    }

    public NowPlayingResponse toDto() {
        if (currentTrack != null)
            return new NowPlayingResponse(
                    currentTrack.id(),
                    currentTrack.title(),
                    currentTrack.artist(),
                    currentTrack.duration(),
                    false
            );
        else return new NowPlayingResponse(
                null,
                null,
                null,
                0,
                false
        );
    }
}
