package com.coradio.rotation.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("rotation.gc")
public record GcProperties(
        Duration processedRetention,
        Duration orphanRetention
) {}