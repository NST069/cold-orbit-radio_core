package com.coradio.rotation.domain.port.out.scrobbler;

import com.coradio.rotation.domain.model.ScrobbleEvent;

public interface ScrobbleEventPublisher {

    void publish(ScrobbleEvent event);
}
