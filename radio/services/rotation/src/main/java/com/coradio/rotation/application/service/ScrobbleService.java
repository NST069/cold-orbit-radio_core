package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.enums.NotificationEvent;
import com.coradio.rotation.domain.model.ScrobbleEvent;
import com.coradio.rotation.domain.model.ScrobbleTrack;
import com.coradio.rotation.domain.port.out.scrobbler.ScrobbleEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
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
        ScrobbleTrack scrobbleTrack = new ScrobbleTrack(
                trackId,
                artist,
                title,
                album,
                duration
        );

        ScrobbleEvent event = new ScrobbleEvent(
                UUID.randomUUID(),
                eventType,
                scrobbleTrack,
                playedAt.getEpochSecond(),
                (eventType == NotificationEvent.NOW_PLAYING) ? (playedAt.getEpochSecond() + scrobbleTrack.duration()) : -1
        );

        log.debug("Sending event {} to scrobblers. eventId: {}", eventType, event.eventId());
        publisher.publish(event);
    }
}
