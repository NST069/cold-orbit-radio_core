package com.coradio.rotation.domain.port.out.redis;

import java.time.Duration;
import java.util.Optional;

public interface RedisCachePort {

    Optional<String> get(String key);

    void put(String key, String value, Duration ttl);

    void put(String key, String value);

    void delete(String key);

}
