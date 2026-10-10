package com.coradio.notification.infrastructure.in.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.redis")
public record RedisStreamProperties(
        Streams streams,
        String consumerGroup,
        String consumerName
) {

    public record Streams(
            String scrobble,
            String errors
    ) {
    }
}
