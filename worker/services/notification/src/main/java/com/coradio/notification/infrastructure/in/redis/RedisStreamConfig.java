package com.coradio.notification.infrastructure.in.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableConfigurationProperties(RedisStreamProperties.class)
public class RedisStreamConfig {

    private final ScrobbleStreamConsumer scrobbleStreamConsumer;

    private final RedisStreamProperties properties;

    @Bean
    StreamMessageListenerContainer<
            String,
            MapRecord<String, String, String>
            > redisStreamMessageListenerContainer(
            RedisConnectionFactory connectionFactory
    ) {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<
                String,
                MapRecord<String, String, String>
                > options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(1))
                        .build();

        return StreamMessageListenerContainer.create(
                connectionFactory,
                options
        );
    }

    @Bean
    RedisStreamInitializer redisStreamInitializer(
            StringRedisTemplate redisTemplate,
            StreamMessageListenerContainer<
                    String,
                    MapRecord<String, String, String>
                    > container
    ) {
        return new RedisStreamInitializer(
                redisTemplate,
                container,
                properties,
                scrobbleStreamConsumer
        );
    }
}
