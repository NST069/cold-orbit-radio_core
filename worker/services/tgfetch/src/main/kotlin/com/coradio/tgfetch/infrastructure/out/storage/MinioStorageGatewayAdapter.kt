package com.coradio.tgfetch.infrastructure.out.storage

import com.coradio.tgfetch.domain.port.out.storage.StorageGatewayPort
import com.coradio.tgfetch.infrastructure.out.storage.config.StorageProperties
import com.coradio.tgfetch.infrastructure.exception.StorageException
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import io.minio.StatObjectArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.tika.Tika
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path

@Component
class MinioStorageGatewayAdapter(
    private val minioClient: MinioClient,
    private val storageProperties: StorageProperties,
): StorageGatewayPort {

    private val log = logger {}
    val tika = Tika()

    override fun upload(key: String, file: Path) {
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(storageProperties.bucket)
                    .`object`(key)
                    .stream(
                        Files.newInputStream(file),
                        Files.size(file),
                        -1
                    )
                    .contentType(tika.detect(file))
                    .build()
            )
            log.debug { "Successfully uploaded: $key" }
        } catch (e: Exception) {
            log.error(e) { "Failed to upload file: $key" }
            throw StorageException(
                "Failed to upload object $key",
                e
            )
        }
    }

    override fun exists(key: String): Boolean {
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(storageProperties.bucket)
                    .`object`(key)
                    .build()
            )
        } catch (_: Exception) {
            return false
        }
        return true
    }

    override fun delete(key: String) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(storageProperties.bucket)
                    .`object`(key)
                    .build()
            )
            log.debug { "Successfully removed $key" }
        } catch (e: Exception) {
            log.warn(e) { "Failed to remove object $key" }
        }
    }
}
