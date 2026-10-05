package com.coradio.notification.infrastructure.in;

import com.coradio.notification.domain.port.model.ScrobbleTrack;
import java.util.UUID;

public interface ProcessScrobbleEventUseCase {

    boolean scrobble(UUID eventId, ScrobbleTrack track, long playedAt);
}
