package com.coradio.rotation.infrastructure.out.storage.config;

import io.minio.MinioClient;
import okhttp3.HttpUrl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        StorageProperties.class,
        StorageProperties.class
})
public class StorageConfiguration {

    @Bean
    public MinioClient minioClient(StorageProperties properties) {
        return MinioClient.builder()
                .endpoint(HttpUrl.parse(properties.endpoint()))
                .credentials(
                        properties.accessKey(),
                        properties.secretKey()
                )
                .build();
    }

}
