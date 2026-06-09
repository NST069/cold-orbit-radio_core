package com.coradio.tgfetch.infrastructure.out.storage.config

import io.minio.MinioClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(StorageProperties::class)
class StorageConfig {

    @Bean
    fun minioClient(
        props: StorageProperties
    ): MinioClient =
        MinioClient.builder()
            .endpoint(props.endpoint)
            .credentials(
                props.accessKey,
                props.secretKey
            )
            .build()
}
