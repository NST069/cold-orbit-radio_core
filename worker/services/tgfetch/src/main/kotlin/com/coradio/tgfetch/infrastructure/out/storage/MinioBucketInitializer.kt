package com.coradio.tgfetch.infrastructure.out.storage

import com.coradio.tgfetch.infrastructure.out.storage.config.StorageProperties
import com.coradio.tgfetch.infrastructure.exception.StorageException
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class MinioBucketInitializer(
    private val minioClient: MinioClient,
    private val storageProperties: StorageProperties,
): ApplicationRunner {

    private val log = logger {}

    override fun run(args: ApplicationArguments) {
        try {
            val bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(storageProperties.bucket)
                    .build()
            )

            if (!bucketExists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(storageProperties.bucket)
                        .build()
                )
                log.info { "Created bucket ${storageProperties.bucket}" }
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to initialize bucket ${storageProperties.bucket}" }
            throw StorageException(
                "Failed to create bucket ${storageProperties.bucket}",
                e
            )
        }

    }
}