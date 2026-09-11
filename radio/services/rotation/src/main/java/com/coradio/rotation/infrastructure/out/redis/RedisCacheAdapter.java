package com.coradio.rotation.infrastructure.out.redis;

import com.coradio.rotation.domain.port.out.redis.RedisCachePort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.Optional;

@Component
public class RedisCacheAdapter implements RedisCachePort {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisCacheAdapter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    @Override
    public void put(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
