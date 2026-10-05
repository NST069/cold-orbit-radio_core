package com.coradio.notification.domain.port.model;

import com.coradio.notification.domain.port.enums.NotificationEvent;

import java.util.UUID;

public record StreamEvent(
        UUID eventId,
        NotificationEvent eventType,
        String payload,
        long playedAt,
        long expiresAt
) {
}
