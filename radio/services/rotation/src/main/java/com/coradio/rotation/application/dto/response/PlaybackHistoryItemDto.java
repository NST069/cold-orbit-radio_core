package com.coradio.rotation.application.dto.response;

import java.time.Instant;

public record PlaybackHistoryItemDto(
        String artist,
        String title,
        Instant playedAt
) {
}
