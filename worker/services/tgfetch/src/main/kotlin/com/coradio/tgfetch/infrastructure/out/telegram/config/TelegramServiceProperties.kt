package com.coradio.tgfetch.infrastructure.out.telegram.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "telegram.service")
data class TelegramServiceProperties(
    val baseUrl: String,
    val connectTimeout: Duration,
    val readTimeout: Duration
)
