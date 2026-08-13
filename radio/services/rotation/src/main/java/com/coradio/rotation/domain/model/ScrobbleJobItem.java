package com.coradio.rotation.domain.model;

import com.coradio.rotation.domain.enums.JobStatus;
import com.coradio.rotation.domain.enums.ScrobblerProvider;
import java.time.Instant;
import java.util.UUID;

public record ScrobbleJobItem(
        UUID id,
        PlaybackHistoryItem playbackHistoryItem,
        ScrobblerProvider provider,
        JobStatus status,
        Instant scheduledAt,
        Instant startedAt,
        Instant sentAt,
        int attempts,
        String error
) {

}
