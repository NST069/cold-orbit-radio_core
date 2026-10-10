package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.enums.NotificationEvent;
import com.coradio.rotation.domain.model.ScrobbleTrack;
import com.coradio.rotation.domain.port.out.scrobbler.ScrobbleEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Slf4j
public class ScrobbleService {

    private final ScrobbleEventPublisher publisher;

    public ScrobbleService(ScrobbleEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(
            NotificationEvent eventType,
            UUID trackId,
            String artist,
            String title,
            String album,
            long duration,
            Instant playedAt
    ) {
        log.debug("Sending event {} to scrobblers. trackId: {}", eventType, trackId);
        publisher.publish(eventType,
                playedAt,
                eventType == NotificationEvent.NOW_PLAYING ? playedAt.plus(duration, ChronoUnit.SECONDS) : null,
                new ScrobbleTrack(
                        trackId,
                        artist,
                        title,
                        album,
                        duration
                ));
    }
}
