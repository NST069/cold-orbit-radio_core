package com.coradio.rotation.domain.model;

import java.time.Instant;
import java.util.UUID;

public record PlaybackHistoryItem(
        UUID id,
        UUID trackId,
        String artist,
        String title,
        String album,
        Instant playedAt,
        int duration
) {

}
