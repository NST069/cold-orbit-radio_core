package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.enums.NotificationEvent;
import com.coradio.rotation.domain.model.ScrobbleEvent;
import com.coradio.rotation.domain.model.ScrobbleTrack;
import com.coradio.rotation.domain.port.out.scrobbler.ScrobbleEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ScrobbleServiceTest {

    @Mock
    private ScrobbleEventPublisher publisher;

    @InjectMocks
    private ScrobbleService service;

    @Test
    void shouldPublishScrobbleEvent() {
        NotificationEvent eventType = NotificationEvent.NOW_PLAYING;
        UUID trackId = UUID.randomUUID();
        String artist = "NiKryukov";
        String title = "Abyss";
        String album = "Aphantasia";
        long duration = 245;
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

        ArgumentCaptor<ScrobbleEvent> captor =
                ArgumentCaptor.forClass(ScrobbleEvent.class);

        verify(publisher).publish(captor.capture());

        ScrobbleEvent actual = captor.getValue();

        assertNotNull(actual.eventId());
        assertEquals(eventType, actual.eventType());
        assertEquals(playedAt.getEpochSecond(), actual.playedAt());

        ScrobbleTrack track = actual.track();

        assertEquals(trackId, track.trackId());
        assertEquals(artist, track.artist());
        assertEquals(title, track.title());
        assertEquals(album, track.album());
        assertEquals(duration, track.duration());

        verifyNoMoreInteractions(publisher);
    }

    @Test
    void shouldCreateScrobbleTrackWithProvidedData() {
        UUID trackId = UUID.randomUUID();

        service.publish(
                NotificationEvent.SCROBBLE,
                trackId,
                "Artist",
                "Title",
                "Album",
                300,
                Instant.now()
        );

        ArgumentCaptor<ScrobbleEvent> captor =
                ArgumentCaptor.forClass(ScrobbleEvent.class);

        verify(publisher).publish(captor.capture());

        ScrobbleTrack track = captor.getValue().track();

        assertEquals(trackId, track.trackId());
        assertEquals("Artist", track.artist());
        assertEquals("Title", track.title());
        assertEquals("Album", track.album());
        assertEquals(300, track.duration());
    }

}
