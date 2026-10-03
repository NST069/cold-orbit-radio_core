package com.coradio.rotation.infrastructure.out.redis;

import com.coradio.rotation.domain.enums.NotificationEvent;
import com.coradio.rotation.domain.model.ScrobbleTrack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RedisScrobbleEventPublisherTest {

    @Mock
    private RedisEventPublisher redisEventPublisher;

    @InjectMocks
    private RedisScrobbleEventPublisher publisher;

    @Test
    void shouldPublishEventWhenScrobblingEnabled() {
        publisher = new RedisScrobbleEventPublisher(redisEventPublisher);
        ReflectionTestUtils.setField(publisher, "enabled", true);

        NotificationEvent eventType = NotificationEvent.SCROBBLE;
        Instant createdAt = Instant.parse("2026-10-03T21:00:00Z");
        Instant expiresAt = null;

        ScrobbleTrack track = new ScrobbleTrack(
                UUID.randomUUID(),
                "Artist",
                "Title",
                "Album",
                245L
        );

        publisher.publish(
                eventType,
                createdAt,
                expiresAt,
                track
        );

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        verify(redisEventPublisher).publishToStream(
                eq(eventType),
                eq("scrobble-events"),
                eq(createdAt),
                isNull(),
                payloadCaptor.capture()
        );

        String payload = payloadCaptor.getValue();

        assertThat(payload)
                .contains("\"trackId\":\"" + track.trackId() + "\"")
                .contains("\"artist\":\"Artist\"")
                .contains("\"title\":\"Title\"")
                .contains("\"album\":\"Album\"")
                .contains("\"duration\":245");
    }

    @Test
    void shouldNotPublishEventWhenScrobblingDisabled() {
        ReflectionTestUtils.setField(publisher, "enabled", false);

        publisher.publish(
                NotificationEvent.SCROBBLE,
                Instant.parse("2026-10-03T21:00:00Z"),
                null,
                new ScrobbleTrack(
                        UUID.randomUUID(),
                        "Artist",
                        "Title",
                        "Album",
                        245L
                )
        );

        verifyNoInteractions(redisEventPublisher);
    }
}
