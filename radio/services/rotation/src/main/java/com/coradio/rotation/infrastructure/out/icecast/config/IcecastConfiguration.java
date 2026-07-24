package com.coradio.rotation.infrastructure.out.icecast.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class IcecastConfiguration {

    @Bean("Icecast")
    RestClient icecastRestClient(IcecastProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.url())
                .build();
    }

}