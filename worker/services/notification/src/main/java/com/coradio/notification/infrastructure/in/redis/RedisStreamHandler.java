package com.coradio.notification.infrastructure.in.redis;

import org.springframework.data.redis.connection.stream.MapRecord;

@FunctionalInterface
public interface RedisStreamHandler {

    boolean handle(MapRecord<String, String, String> message);
}
