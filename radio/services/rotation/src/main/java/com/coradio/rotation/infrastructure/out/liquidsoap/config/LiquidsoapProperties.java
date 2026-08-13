package com.coradio.rotation.infrastructure.out.liquidsoap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("liquidsoap")
public record LiquidsoapProperties(
        String host,
        int port,
        int timeout
) {
}
