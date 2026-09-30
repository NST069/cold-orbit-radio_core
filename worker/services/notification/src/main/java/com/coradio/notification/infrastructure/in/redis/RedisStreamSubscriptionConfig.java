package com.coradio.notification.infrastructure.in.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RedisStreamSubscriptionConfig {

    private final RedisStreamListener listener;
    private final RedisStreamProperties properties;
    private final RedisStreamHandler redisStreamHandler;

    @Bean
    ApplicationRunner subscribeToRedisStreams() {
        return args -> {
            listener.listen(
                    properties.streams().scrobble(),
                    properties.consumerGroup(),
                    properties.consumerName(),
                    redisStreamHandler
            );

            listener.start();
        };
    }
}
