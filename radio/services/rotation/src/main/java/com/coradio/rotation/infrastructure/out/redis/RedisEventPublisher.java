package com.coradio.rotation.infrastructure.out.redis;

import com.coradio.rotation.domain.enums.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisEventPublisher {

    private final RedisTemplate<String, String> redisTemplate;

    public void publishToStream(NotificationEvent eventType, String stream, Instant createdAt, Instant expiresAt, String payload) {
        publishToStream(eventType, stream, createdAt, expiresAt, payload, UUID.randomUUID());
    }

    void publishToStream(NotificationEvent eventType, String stream, Instant createdAt, Instant expiresAt, String payload, UUID eventId) {
        redisTemplate.opsForStream().add(
                stream,
                Map.of("type", eventType.name(),
                        "eventId", eventId.toString(),
                        "payload", payload,
                        "createdAt", "" + createdAt.getEpochSecond(),
                        "expiresAt", "" + (expiresAt != null ? expiresAt.getEpochSecond() : -1)
                )
        );
    }
}
