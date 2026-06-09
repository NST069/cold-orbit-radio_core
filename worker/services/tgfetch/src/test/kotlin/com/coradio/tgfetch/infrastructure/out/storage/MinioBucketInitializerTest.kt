package com.coradio.tgfetch.infrastructure.out.storage

import com.coradio.tgfetch.infrastructure.exception.StorageException
import com.coradio.tgfetch.infrastructure.out.storage.config.StorageProperties
import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class MinioBucketInitializerTest {

    @Mock
    lateinit var minioClient: MinioClient

    lateinit var initializer: MinioBucketInitializer

    @BeforeEach
    fun setup() {
        initializer = MinioBucketInitializer(
            minioClient,
            StorageProperties(
                endpoint = "endpoint",
                accessKey = "access-key",
                secretKey = "secret-key",
                bucket = "radio",
            )
        )
    }

    @Test
    fun `should do nothing when bucket already exists`() {

        whenever(
            minioClient.bucketExists(any<BucketExistsArgs>())
        ).thenReturn(true)

        initializer.run(mock())

        verify(minioClient, never())
            .makeBucket(any<MakeBucketArgs>())
    }

    @Test
    fun `should create bucket when it does not exist`() {

        whenever(
            minioClient.bucketExists(any<BucketExistsArgs>())
        ).thenReturn(false)

        initializer.run(mock())

        verify(minioClient)
            .makeBucket(any<MakeBucketArgs>())
    }

    @Test
    fun `should throw storage exception when minio fails`() {

        whenever(
            minioClient.bucketExists(any<BucketExistsArgs>())
        ).thenThrow(RuntimeException("boom"))

        val ex = assertThrows<StorageException> {
            initializer.run(mock())
        }

        assertTrue(
            ex.message!!.contains("Failed to create bucket")
        )

        assertNotNull(ex.cause)
    }
}
