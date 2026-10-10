package com.coradio.rotation.domain.port.out.scrobbler;

import com.coradio.rotation.domain.enums.NotificationEvent;
import com.coradio.rotation.domain.model.ScrobbleTrack;
import java.time.Instant;

public interface ScrobbleEventPublisher {

    void publish(NotificationEvent eventType, Instant createdAt, Instant expiresAt, ScrobbleTrack track);
}
