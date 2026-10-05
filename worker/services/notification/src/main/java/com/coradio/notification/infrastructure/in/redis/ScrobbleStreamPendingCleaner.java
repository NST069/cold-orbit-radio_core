package com.coradio.notification.infrastructure.in.redis;

import com.coradio.notification.domain.port.out.RedisStreamPendingCleanerPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScrobbleStreamPendingCleaner implements RedisStreamPendingCleanerPort {

    private static final Duration MAX_PENDING_IDLE = Duration.ofHours(1);
    private static final int BATCH_SIZE = 1000;

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisStreamProperties properties;

    public void cleanup() {
        String stream = properties.streams().scrobble();
        String group = properties.consumerGroup();

        log.debug("Starting pending cleanup: stream={}, group={}", stream, group);

        List<PendingMessage> pending = redisTemplate.opsForStream()
                .pending(
                        stream,
                        group,
                        Range.unbounded(),
                        BATCH_SIZE
                )
                .stream()
                .filter(message ->
                        message.getElapsedTimeSinceLastDelivery().compareTo(MAX_PENDING_IDLE) >= 0
                )
                .toList();

        log.debug("Found {} pending messages older than {}", pending.size(), MAX_PENDING_IDLE);

        pending.forEach(message ->
                acknowledgeAndDelete(stream, group, message)
        );

        log.debug("Pending cleanup finished");
    }

    private void acknowledgeAndDelete(
            String stream,
            String group,
            PendingMessage message
    ) {
        String messageId = message.getIdAsString();

        try {
            Long acknowledged = redisTemplate.opsForStream()
                    .acknowledge(
                            stream,
                            group,
                            message.getId()
                    );

            if (!Long.valueOf(1).equals(acknowledged)) {
                log.warn("Failed to ACK pending message: id={}, result={}", messageId, acknowledged);
                return;
            }

            Long deleted = redisTemplate.opsForStream().delete(stream, message.getId());

            log.info("Dropped stale pending message: id={}, idle={}, deleted={}", messageId, message.getElapsedTimeSinceLastDelivery(), deleted);

        } catch (Exception e) {
            log.error("Failed to drop pending message: id={}", messageId, e);
        }
    }
}
