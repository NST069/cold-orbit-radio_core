package com.coradio.tgfetch.infrastructure.out.redis

import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class RedisConfiguration(
    @Value("\${spring.data.redis.ttl}")
    private val ttl: Duration
) {

    @Bean
    fun cacheManager(
        connectionFactory: RedisConnectionFactory
    ): RedisCacheManager {
        val valueSerializer =
            RedisSerializationContext.SerializationPair.fromSerializer(
                GenericJacksonJsonRedisSerializer.builder()
                    .enableUnsafeDefaultTyping()
                    .build()
            )

        val config =
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair
                        .fromSerializer(StringRedisSerializer())
                )
                .serializeValuesWith(valueSerializer)
                .disableCachingNullValues()

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build()
    }
}
