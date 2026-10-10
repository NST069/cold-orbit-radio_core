package com.coradio.rotation.infrastructure.out.scrobble.lastfm;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(LastFmProperties.class)
public class LastFmConfig {

    @Bean("LastFm")
    RestClient lastFmRestClient(LastFmProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(Duration.ofSeconds(15));

        return RestClient.builder()
                .baseUrl(properties.apiUrl())
                .requestFactory(requestFactory)
                .build();
    }

}
