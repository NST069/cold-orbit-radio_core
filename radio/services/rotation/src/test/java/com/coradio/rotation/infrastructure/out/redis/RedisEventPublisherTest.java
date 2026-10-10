package com.coradio.rotation.infrastructure.out.redis;

import com.coradio.rotation.domain.enums.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisEventPublisherTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @InjectMocks
    private RedisEventPublisher publisher;

    @BeforeEach
    void setUp() {

        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
    }

    @Test
    void shouldPublishEventToStream() {
        NotificationEvent eventType = NotificationEvent.SCROBBLE;
        String stream = "scrobble-events";
        Instant createdAt = Instant.parse("2026-10-03T21:00:00Z");
        Instant expiresAt = Instant.parse("2026-10-03T21:04:05Z");
        String payload = "{\"trackId\":\"123\"}";

        publisher.publishToStream(
                eventType,
                stream,
                createdAt,
                expiresAt,
                payload
        );

        verify(streamOperations).add(
                eq(stream),
                eq(Map.of(
                        "type", eventType.name(),
                        "payload", payload,
                        "createdAt", createdAt.getEpochSecond(),
                        "expiresAt", expiresAt.getEpochSecond()
                ))
        );
    }

    @Test
    void shouldUseMinusOneWhenExpiresAtIsNull() {
        NotificationEvent eventType = NotificationEvent.NOW_PLAYING;
        String stream = "scrobble-events";
        Instant createdAt = Instant.parse("2026-10-03T21:00:00Z");
        String payload = "{\"trackId\":\"123\"}";

        publisher.publishToStream(
                eventType,
                stream,
                createdAt,
                null,
                payload
        );

        verify(streamOperations).add(
                eq(stream),
                eq(Map.of(
                        "type", eventType.name(),
                        "payload", payload,
                        "createdAt", createdAt.getEpochSecond(),
                        "expiresAt", -1L
                ))
        );
    }

}
