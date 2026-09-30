package com.coradio.notification.domain.port.model;

import com.coradio.notification.domain.port.enums.NotificationEvent;

import java.util.UUID;

public record ScrobbleEvent(
        UUID eventId,
        NotificationEvent eventType,
        ScrobbleTrack track,
        long playedAt,
        long expiresAt
) {
}
