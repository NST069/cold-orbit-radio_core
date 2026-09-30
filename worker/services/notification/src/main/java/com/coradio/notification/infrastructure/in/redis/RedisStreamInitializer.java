package com.coradio.notification.infrastructure.in.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

@Slf4j
public class RedisStreamInitializer {

    private final StringRedisTemplate redisTemplate;

    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    private final RedisStreamProperties properties;

    private final ScrobbleStreamConsumer scrobbleStreamConsumer;

    public RedisStreamInitializer(
            StringRedisTemplate redisTemplate,
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> container,
            RedisStreamProperties properties,
            ScrobbleStreamConsumer scrobbleStreamConsumer
    ) {
        this.redisTemplate = redisTemplate;
        this.container = container;
        this.properties = properties;
        this.scrobbleStreamConsumer = scrobbleStreamConsumer;
    }

    public void initialize() {
        createConsumerGroup();

        subscribeToScrobbleStream();

        container.start();

        log.info("Redis stream listener started");
    }

    private void createConsumerGroup() {
        String stream = properties.streams().scrobble();
        String group = properties.consumerGroup();

        try {
            redisTemplate.opsForStream()
                    .createGroup(
                            stream,
                            ReadOffset.from("0-0"),
                            group
                    );

            log.info("Created Redis consumer group {} for stream {}", group, stream
            );

        } catch (Exception e) {
            if (isGroupAlreadyExists(e)) {
                log.debug("Redis consumer group {} already exists for stream {}", group, stream);
                return;
            }

            throw e;
        }
    }

    private void subscribeToScrobbleStream() {
        String stream = properties.streams().scrobble();
        String group = properties.consumerGroup();
        String consumer = properties.consumerName();

        log.info("Subscribing to Redis stream {} with group {} and consumer {}", stream, group, consumer);

        container.receive(
                Consumer.from(group, consumer),
                StreamOffset.create(
                        stream,
                        ReadOffset.lastConsumed()
                ),
                scrobbleStreamConsumer::handle
        );
    }

    private boolean isGroupAlreadyExists(Exception exception) {
        Throwable current = exception;

        while (current != null) {
            if (current.getMessage() != null && current.getMessage().contains("BUSYGROUP")) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}
