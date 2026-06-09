package com.coradio.tgfetch.infrastructure.out.storage.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("storage")
data class StorageProperties(
    val endpoint: String,
    val accessKey: String,
    val secretKey: String,
    val bucket: String,
)
