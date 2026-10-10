package com.coradio.notification.domain.port.out;

public interface RedisStreamPendingCleanerPort {

    void cleanup();
}
