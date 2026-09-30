package com.coradio.notification.infrastructure.in.redis;

import com.coradio.notification.domain.port.model.ScrobbleEvent;
import com.coradio.notification.infrastructure.in.ProcessNowPlayingEventUseCase;
import com.coradio.notification.infrastructure.in.ProcessScrobbleEventUseCase;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScrobbleStreamConsumer implements RedisStreamHandler {

    private static final String PAYLOAD_FIELD = "payload";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ProcessScrobbleEventUseCase processScrobbleEventService;

    private final ProcessNowPlayingEventUseCase processNowPlayingEventService;

    private ScrobbleEvent deserialize(String payload) {
        try {
            return objectMapper.readValue(
                    payload,
                    ScrobbleEvent.class
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize scrobble event", e);
        }
    }

    @Override
    public boolean handle(MapRecord<String, String, String> message) {
        log.debug("Caught message {}", message);

        String payload = message.getValue().get(PAYLOAD_FIELD);

        if (payload == null) throw new IllegalArgumentException("Scrobble event does not contain payload");

        ScrobbleEvent event = deserialize(payload);

        boolean isSuccess = false;

        switch (event.eventType()) {
            case SCROBBLE -> isSuccess = processScrobbleEventService.scrobble(event);
            case NOW_PLAYING -> isSuccess = processNowPlayingEventService.update(event);
        }

        return isSuccess;
    }
}
