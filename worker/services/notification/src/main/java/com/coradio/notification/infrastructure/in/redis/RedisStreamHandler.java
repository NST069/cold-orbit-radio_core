package com.coradio.notification.infrastructure.in.redis;

import org.springframework.data.redis.connection.stream.MapRecord;

public interface RedisStreamHandler {

    String getStream();

    boolean handle(MapRecord<String, String, String> message);
}
