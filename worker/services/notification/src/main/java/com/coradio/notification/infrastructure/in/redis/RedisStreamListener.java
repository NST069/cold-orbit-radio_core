package com.coradio.notification.infrastructure.in.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisStreamListener {

    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    private final RedisTemplate<String, String> redisTemplate;

    public void listen(
            String stream,
            String consumerGroup,
            String consumerName,
            RedisStreamHandler handler
    ) {
        container.receive(
                Consumer.from(
                        consumerGroup,
                        consumerName
                ),
                StreamOffset.create(
                        stream,
                        ReadOffset.lastConsumed()
                ),
                message -> {
                    log.debug("Message Received: id={}, value={}", message.getId(), message.getValue());

                    try {
                        boolean isSuccess = handler.handle(message);

                        redisTemplate.opsForStream().acknowledge(
                                stream,
                                consumerGroup,
                                message.getId()
                        );
                        redisTemplate.opsForStream().delete(
                                stream,
                                message.getId()
                        );
                        log.debug("Message Processed: id={}, successful:{}", message.getId(), isSuccess);
                    } catch (Exception e) {
                        log.error("Handler Failed: id={}", message.getId(), e);
                    }
                }
        );
    }

    public void start() {
        container.start();
    }
}
