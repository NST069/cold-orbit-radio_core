package com.coradio.rotation.domain.model;

import com.coradio.rotation.domain.enums.PlaybackStatus;
import java.time.Instant;
import java.util.UUID;

public record TrackQueueItem(
        UUID id,
        UUID trackId,
        PlaybackStatus status,
        String localPath,
        Instant createdAt,
        Instant playedAt
) {
}
