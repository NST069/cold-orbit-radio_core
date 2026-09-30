package com.coradio.notification.infrastructure.in;

import com.coradio.notification.domain.port.model.ScrobbleEvent;

public interface ProcessScrobbleEventUseCase {

    boolean scrobble(ScrobbleEvent event);
}
