package com.coradio.rotation.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("rotation.playback")
public record PlaybackProperties(
        int minSize,
        int targetSize
) {
}
