package com.coradio.notification.infrastructure.in;

import com.coradio.notification.domain.port.model.ScrobbleEvent;

public interface ProcessNowPlayingEventUseCase {

    boolean update(ScrobbleEvent event);
}
