package com.coradio.rotation.infrastructure.out.icecast.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("icecast")
public record IcecastProperties(
        String url,
        String mountPoint
) {
}
