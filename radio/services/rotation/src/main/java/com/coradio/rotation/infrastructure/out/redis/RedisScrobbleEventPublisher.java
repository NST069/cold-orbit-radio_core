package com.coradio.rotation.infrastructure.out.redis;

import com.coradio.rotation.domain.enums.NotificationEvent;
import com.coradio.rotation.domain.model.ScrobbleTrack;
import com.coradio.rotation.domain.port.out.scrobbler.ScrobbleEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisScrobbleEventPublisher implements ScrobbleEventPublisher {

    private static final String STREAM = "scrobble-events";

    @Value("${scrobble.enabled}")
    private boolean enabled;

    private final RedisEventPublisher redisEventPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void publish(NotificationEvent eventType, Instant createdAt, Instant expiresAt, ScrobbleTrack track) {
        if(!enabled){
            log.info("Skipped scrobble event {}. Scrobbling is disabled", track.trackId());
            return;
        }
        try {
            String payload = objectMapper.writeValueAsString(track);

            redisEventPublisher.publishToStream(eventType, STREAM, createdAt, expiresAt, payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to publish scrobble event", e);
        }
    }
}
