package com.coradio.tgfetch.infrastructure.out.telegram.config

import com.coradio.tgfetch.infrastructure.exception.TelegramException
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.client.WebClient
import java.net.http.HttpClient

@Configuration
@EnableConfigurationProperties(TelegramServiceProperties::class)
class TelegramClientConfig {

    @Bean
    fun telegramRestClient(
        properties: TelegramServiceProperties
    ): RestClient {
        try {
            val httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout)
                .build()

            return RestClient.builder()
                .baseUrl(properties.baseUrl)
                .requestFactory(
                    JdkClientHttpRequestFactory(httpClient)
                )
                .build()
        } catch (ex: Exception) {
            throw TelegramException("Telegram service is unavailable", ex)
        }
    }

    @Bean
    fun telegramWebClient(
        properties: TelegramServiceProperties
    ): WebClient =
        WebClient.builder()
            .baseUrl(properties.baseUrl)
            .build()
}
