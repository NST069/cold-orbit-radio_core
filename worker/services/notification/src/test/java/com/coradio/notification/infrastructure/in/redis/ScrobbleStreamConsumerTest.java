package com.coradio.notification.infrastructure.in.redis;

import com.coradio.notification.domain.enums.NotificationEvent;
import com.coradio.notification.domain.model.ScrobbleTrack;
import com.coradio.notification.domain.model.StreamEvent;
import com.coradio.notification.domain.port.in.ProcessNowPlayingEventUseCase;
import com.coradio.notification.domain.port.in.ProcessScrobbleEventUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.MapRecord;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScrobbleStreamConsumerTest {

    @Mock
    private RedisStreamConsumer consumer;

    @Mock
    private ProcessScrobbleEventUseCase processScrobbleEventService;

    @Mock
    private ProcessNowPlayingEventUseCase processNowPlayingEventService;

    @Mock
    private RedisStreamProperties properties;

    @Mock
    private RedisStreamProperties.Streams streams;

    @Mock
    private MapRecord<String, String, String> message;

    @Mock
    private StreamEvent event;

    @InjectMocks
    private ScrobbleStreamConsumer scrobbleStreamConsumer;

    @Test
    void getStream_shouldReturnScrobbleStream() {
        when(properties.streams()).thenReturn(streams);
        when(streams.scrobble()).thenReturn("scrobble-events");

        assertThat(scrobbleStreamConsumer.getStream()).isEqualTo("scrobble-events");
    }

    @Test
    void handle_shouldProcessScrobbleEvent() {
        UUID eventId = UUID.fromString("24ed6cd9-dcd1-4842-8d83-23f46eeba7e8");
        String payload = """
                {
                    "trackId": "24ed6cd9-dcd1-4842-8d83-23f46eeba7e9",
                    "artist": "Test Artist",
                    "title": "Test Track",
                    "album": "Test Album",
                    "duration": 240
                }
                """;

        long playedAt = Instant.parse("2026-10-07T18:00:00Z").getEpochSecond();

        when(consumer.handle(message)).thenReturn(event);
        when(event.eventType()).thenReturn(NotificationEvent.SCROBBLE);
        when(event.eventId()).thenReturn(eventId);
        when(event.payload()).thenReturn(payload);
        when(event.playedAt()).thenReturn(playedAt);

        when(processScrobbleEventService.scrobble(
                eq(eventId),
                any(ScrobbleTrack.class),
                eq(playedAt)
        )).thenReturn(true);

        boolean result = scrobbleStreamConsumer.handle(message);

        assertThat(result).isTrue();

        verify(consumer).handle(message);

        verify(processScrobbleEventService).scrobble(
                eq(eventId),
                any(ScrobbleTrack.class),
                eq(playedAt)
        );

        verify(processNowPlayingEventService, never()).update(
                any(),
                any(),
                anyLong()
        );
    }

    @Test
    void handle_shouldProcessNowPlayingEvent() {
        UUID eventId = UUID.fromString("24ed6cd9-dcd1-4842-8d83-23f46eeba7e8");
        String payload = """
                {
                    "trackId": "24ed6cd9-dcd1-4842-8d83-23f46eeba7e9",
                    "artist": "Test Artist",
                    "title": "Test Track",
                    "album": "Test Album",
                    "duration": 240
                }
                """;

        long expiresAt = Instant.parse("2026-10-07T18:04:00Z").getEpochSecond();

        when(consumer.handle(message)).thenReturn(event);
        when(event.eventType()).thenReturn(NotificationEvent.NOW_PLAYING);
        when(event.eventId()).thenReturn(eventId);
        when(event.payload()).thenReturn(payload);
        when(event.expiresAt()).thenReturn(expiresAt);

        when(processNowPlayingEventService.update(
                eq(eventId),
                any(ScrobbleTrack.class),
                eq(expiresAt)
        )).thenReturn(true);

        boolean result = scrobbleStreamConsumer.handle(message);

        assertThat(result).isTrue();

        verify(consumer).handle(message);

        verify(processNowPlayingEventService).update(
                eq(eventId),
                any(ScrobbleTrack.class),
                eq(expiresAt)
        );

        verify(processScrobbleEventService, never()).scrobble(
                any(),
                any(),
                anyLong()
        );
    }

    @Test
    void handle_shouldReturnFalseWhenScrobbleProcessingFails() {
        UUID eventId = UUID.fromString("24ed6cd9-dcd1-4842-8d83-23f46eeba7e8");
        String payload = """
                {
                    "trackId": "24ed6cd9-dcd1-4842-8d83-23f46eeba7e9",
                    "artist": "Test Artist",
                    "title": "Test Track",
                    "album": "Test Album",
                    "duration": 240
                }
                """;

        long playedAt = Instant.parse("2026-10-07T18:00:00Z").getEpochSecond();

        when(consumer.handle(message)).thenReturn(event);
        when(event.eventType()).thenReturn(NotificationEvent.SCROBBLE);
        when(event.eventId()).thenReturn(eventId);
        when(event.payload()).thenReturn(payload);
        when(event.playedAt()).thenReturn(playedAt);

        when(processScrobbleEventService.scrobble(
                any(),
                any(),
                anyLong()
        )).thenReturn(false);

        boolean result = scrobbleStreamConsumer.handle(message);

        assertThat(result).isFalse();
    }

    @Test
    void handle_shouldReturnFalseWhenNowPlayingProcessingFails() {
        UUID eventId = UUID.fromString("24ed6cd9-dcd1-4842-8d83-23f46eeba7e8");
        String payload = """
                {
                    "trackId": "24ed6cd9-dcd1-4842-8d83-23f46eeba7e9",
                    "artist": "Test Artist",
                    "title": "Test Track",
                    "album": "Test Album",
                    "duration": 240
                }
                """;

        long expiresAt = Instant.parse("2026-10-07T18:04:00Z").getEpochSecond();

        when(consumer.handle(message)).thenReturn(event);
        when(event.eventType()).thenReturn(NotificationEvent.NOW_PLAYING);
        when(event.eventId()).thenReturn(eventId);
        when(event.payload()).thenReturn(payload);
        when(event.expiresAt()).thenReturn(expiresAt);

        when(processNowPlayingEventService.update(
                any(),
                any(),
                anyLong()
        )).thenReturn(false);

        boolean result = scrobbleStreamConsumer.handle(message);

        assertThat(result)
                .isFalse();
    }

    @Test
    void handle_shouldThrowIllegalArgumentExceptionWhenPayloadCannotBeDeserialized() {
        when(consumer.handle(message)).thenReturn(event);
        when(event.payload()).thenReturn("invalid-json");

        assertThatThrownBy(() -> scrobbleStreamConsumer.handle(message))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Failed to deserialize scrobble event")
                .hasCauseInstanceOf(Exception.class);

        verifyNoInteractions(
                processScrobbleEventService,
                processNowPlayingEventService
        );
    }

    @Test
    void handle_shouldDeserializePayloadAndPassTrackToScrobbleService() {
        UUID eventId = UUID.fromString("24ed6cd9-dcd1-4842-8d83-23f46eeba7e8");
        String payload = """
                {
                    "trackId": "24ed6cd9-dcd1-4842-8d83-23f46eeba7e9",
                    "artist": "Test Artist",
                    "title": "Test Track",
                    "album": "Test Album",
                    "duration": 240
                }
                """;

        long playedAt = Instant.parse("2026-10-07T18:00:00Z").getEpochSecond();

        when(consumer.handle(message)).thenReturn(event);
        when(event.eventType()).thenReturn(NotificationEvent.SCROBBLE);
        when(event.eventId()).thenReturn(eventId);
        when(event.payload()).thenReturn(payload);
        when(event.playedAt()).thenReturn(playedAt);

        when(processScrobbleEventService.scrobble(
                any(),
                any(ScrobbleTrack.class),
                anyLong()
        )).thenReturn(true);

        scrobbleStreamConsumer.handle(message);

        ArgumentCaptor<ScrobbleTrack> trackCaptor = ArgumentCaptor.forClass(ScrobbleTrack.class);

        verify(processScrobbleEventService).scrobble(
                eq(eventId),
                trackCaptor.capture(),
                eq(playedAt)
        );

        ScrobbleTrack actualTrack = trackCaptor.getValue();

        assertThat(actualTrack).isNotNull();
        assertThat(actualTrack.trackId()).isEqualTo(UUID.fromString("24ed6cd9-dcd1-4842-8d83-23f46eeba7e9"));
        assertThat(actualTrack.artist()).isEqualTo("Test Artist");
        assertThat(actualTrack.title()).isEqualTo("Test Track");
        assertThat(actualTrack.album()).isEqualTo("Test Album");
        assertThat(actualTrack.duration()).isEqualTo(240L);
    }
}
