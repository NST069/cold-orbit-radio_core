package com.coradio.rotation.infrastructure.out.redis;

import com.coradio.rotation.domain.model.ScrobbleEvent;
import com.coradio.rotation.domain.port.out.scrobbler.ScrobbleEventPublisher;
import com.coradio.rotation.infrastructure.exception.ScrobbleEventPublishingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisScrobbleEventPublisher implements ScrobbleEventPublisher {

    private static final String STREAM = "scrobble-events";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void publish(ScrobbleEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            redisTemplate.opsForStream().add(
                    STREAM,
                    Map.of("payload", payload)
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to publish scrobble event", e);
            //throw new ScrobbleEventPublishingException("Failed to publish scrobble event: " + e.getMessage());
        }
    }
}
