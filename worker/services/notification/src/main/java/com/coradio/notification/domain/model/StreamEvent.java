package com.coradio.notification.domain.model;

import com.coradio.notification.domain.enums.NotificationEvent;

import java.util.UUID;

public record StreamEvent(
        UUID eventId,
        NotificationEvent eventType,
        String payload,
        long playedAt,
        long expiresAt
) {
}
