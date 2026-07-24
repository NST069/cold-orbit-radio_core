package com.coradio.rotation.domain.context;

import lombok.Builder;
import java.time.Instant;

@Builder
public record StationInfo(

        String name,
        String description,
        String genre,
        String url,

        String currentSong,

        Integer listeners,
        Integer peakListeners,

        Instant streamStarted

) {}
