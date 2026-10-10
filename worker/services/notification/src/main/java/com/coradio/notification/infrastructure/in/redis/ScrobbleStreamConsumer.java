package com.coradio.notification.infrastructure.in.redis;

import com.coradio.notification.domain.model.ScrobbleTrack;
import com.coradio.notification.domain.model.StreamEvent;
import com.coradio.notification.domain.port.in.ProcessNowPlayingEventUseCase;
import com.coradio.notification.domain.port.in.ProcessScrobbleEventUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScrobbleStreamConsumer implements RedisStreamHandler {

    private final RedisStreamConsumer consumer;

    private final ProcessScrobbleEventUseCase processScrobbleEventService;

    private final ProcessNowPlayingEventUseCase processNowPlayingEventService;

    private final RedisStreamProperties properties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ScrobbleTrack deserialize(String payload) {
        try {
            return objectMapper.readValue(
                    payload,
                    ScrobbleTrack.class
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize scrobble event", e);
        }
    }

    @Override
    public String getStream() {
        return properties.streams().scrobble();
    }

    @Override
    public boolean handle(MapRecord<String, String, String> message) {

        StreamEvent event = consumer.handle(message);

        ScrobbleTrack track = deserialize(event.payload());

        boolean isSuccess = false;

        switch (event.eventType()) {
            case SCROBBLE -> isSuccess = processScrobbleEventService.scrobble(event.eventId(), track, event.playedAt());
            case NOW_PLAYING -> isSuccess = processNowPlayingEventService.update(event.eventId(), track, event.expiresAt());
        }

        return isSuccess;
    }
}
