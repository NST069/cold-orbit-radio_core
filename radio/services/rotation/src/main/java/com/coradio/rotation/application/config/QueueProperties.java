package com.coradio.rotation.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("rotation.queue")
public record QueueProperties(
        int minSize,
        int targetSize,
        int historyHours
) {
}
