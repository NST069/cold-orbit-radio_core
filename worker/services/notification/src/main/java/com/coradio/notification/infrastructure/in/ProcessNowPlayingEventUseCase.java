package com.coradio.notification.infrastructure.in;

import com.coradio.notification.domain.port.model.ScrobbleTrack;
import java.util.UUID;

public interface ProcessNowPlayingEventUseCase {

    boolean update(UUID eventId, ScrobbleTrack track, long expiresAt);
}
