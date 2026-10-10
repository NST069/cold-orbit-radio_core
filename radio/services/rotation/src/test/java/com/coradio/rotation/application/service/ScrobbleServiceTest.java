package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.enums.NotificationEvent;
import com.coradio.rotation.domain.model.ScrobbleTrack;
import com.coradio.rotation.domain.port.out.scrobbler.ScrobbleEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScrobbleServiceTest {

    @Mock
    private ScrobbleEventPublisher publisher;

    @InjectMocks
    private ScrobbleService service;

    @Test
    void shouldPublishNowPlayingEventWithExpiration() {
        NotificationEvent eventType = NotificationEvent.NOW_PLAYING;
        UUID trackId = UUID.randomUUID();
        String artist = "NiKryukov";
        String title = "Abyss";
        String album = "Aphantasia";
        long duration = 245L;
        Instant playedAt = Instant.parse("2026-09-13T20:00:00Z");

        service.publish(
                eventType,
                trackId,
                artist,
                title,
                album,
                duration,
                playedAt
        );

        ScrobbleTrack expectedTrack = new ScrobbleTrack(
                trackId,
                artist,
                title,
                album,
                duration
        );

        verify(publisher).publish(
                eventType,
                playedAt,
                playedAt.plus(duration, ChronoUnit.SECONDS),
                expectedTrack
        );
    }

    @Test
    void shouldPublishScrobbleEventWithoutExpiration() {
        NotificationEvent eventType = NotificationEvent.SCROBBLE;
        UUID trackId = UUID.randomUUID();
        String artist = "NiKryukov";
        String title = "Abyss";
        String album = "Aphantasia";
        long duration = 245L;
        Instant playedAt = Instant.parse("2026-09-13T20:00:00Z");

        service.publish(
                eventType,
                trackId,
                artist,
                title,
                album,
                duration,
                playedAt
        );

        ScrobbleTrack expectedTrack = new ScrobbleTrack(
                trackId,
                artist,
                title,
                album,
                duration
        );

        verify(publisher).publish(
                eventType,
                playedAt,
                null,
                expectedTrack
        );
    }

}
