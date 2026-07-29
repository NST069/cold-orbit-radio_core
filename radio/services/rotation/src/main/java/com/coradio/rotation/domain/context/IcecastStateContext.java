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

    public void setStationInfo(StationInfo stationInfo) {
        this.stationInfo = stationInfo;
        this.available = true;
        this.updatedAt = Instant.now();
    }

    public RadioInfoResponse toDto() {
        if (stationInfo != null)
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
        else return new RadioInfoResponse(
                "Cold Orbit Radio",
                "",
                "",
                "",
                0,
                0,
                "No song played",
                null
        );
    }
}
