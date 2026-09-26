package com.coradio.rotation.domain.model;

import com.coradio.rotation.domain.enums.NotificationEvent;
import java.util.UUID;

public record ScrobbleEvent(
        UUID eventId,
        NotificationEvent eventType,
        ScrobbleTrack track,
        long playedAt,
        long expiresAt
) {
}
