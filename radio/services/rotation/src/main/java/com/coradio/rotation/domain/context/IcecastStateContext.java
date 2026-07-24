package com.coradio.rotation.domain.context;

import com.coradio.rotation.application.dto.response.RadioInfoResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
@Getter
@Setter
public class IcecastStateContext {

    private volatile StationInfo stationInfo;

    private Instant updatedAt;

    private boolean available = false;

    public RadioInfoResponse toDto() {
        return new RadioInfoResponse(
                stationInfo.name(),
                stationInfo.description(),
                stationInfo.genre(),
                stationInfo.url(),
                stationInfo.listeners() == null ? 0 : stationInfo.listeners(),
                stationInfo.peakListeners() == null ? 0 : stationInfo.peakListeners(),
                stationInfo.currentSong() == null || stationInfo.currentSong().isBlank() ? "No song played" : stationInfo.currentSong(),
                stationInfo.streamStarted().toString()
        );
    }
}
