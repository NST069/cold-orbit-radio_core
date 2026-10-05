package com.coradio.notification.domain.port.in;

import com.coradio.notification.domain.model.ScrobbleTrack;
import java.util.UUID;

public interface ProcessScrobbleEventUseCase {

    boolean scrobble(UUID eventId, ScrobbleTrack track, long playedAt);
}
