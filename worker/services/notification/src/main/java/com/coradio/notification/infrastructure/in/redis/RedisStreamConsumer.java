package com.coradio.notification.infrastructure.in.redis;

import com.coradio.notification.domain.port.enums.NotificationEvent;
import com.coradio.notification.domain.port.model.StreamEvent;
import com.coradio.notification.infrastructure.exception.UnimplementedEventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisStreamConsumer {

    private static final String EVENT_ID_FIELD = "eventId";
    private static final String TYPE_FIELD = "type";
    private static final String PAYLOAD_FIELD = "payload";
    private static final String CREATED_AT_FIELD = "createdAt";
    private static final String EXPIRES_AT_FIELD = "expiresAt";

    public StreamEvent handle(MapRecord<String, String, String> message) {
        log.debug("Caught message {}", message);

        UUID eventId = UUID.fromString(message.getValue().get(EVENT_ID_FIELD));
        String type = message.getValue().get(TYPE_FIELD);
        String payload = message.getValue().get(PAYLOAD_FIELD);
        long createdAt = Long.parseLong(message.getValue().get(CREATED_AT_FIELD));
        long expiresAt = Long.parseLong(message.getValue().get(EXPIRES_AT_FIELD));

        if(Arrays.stream(NotificationEvent.values()).noneMatch(event -> event.name().equals(type)))
            throw new UnimplementedEventException(type);

        if (payload == null) throw new IllegalArgumentException("Event does not contain payload");

        return new StreamEvent(eventId, NotificationEvent.valueOf(type), payload, createdAt, expiresAt);
    }
}
