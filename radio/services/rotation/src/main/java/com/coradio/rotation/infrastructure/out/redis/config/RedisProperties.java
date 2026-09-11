package com.coradio.rotation.infrastructure.out.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties("redis")
public record RedisProperties(
        String host,
        int port,
        String password,
        Duration timeout,
        Duration ttl
) {
}
