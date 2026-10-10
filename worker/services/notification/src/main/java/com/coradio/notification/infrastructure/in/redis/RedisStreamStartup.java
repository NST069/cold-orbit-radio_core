package com.coradio.notification.infrastructure.in.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RedisStreamStartup {

    private final RedisStreamInitializer initializer;

    @Bean
    ApplicationRunner streamStartup() {
        return args -> initializer.initialize();
    }
}
