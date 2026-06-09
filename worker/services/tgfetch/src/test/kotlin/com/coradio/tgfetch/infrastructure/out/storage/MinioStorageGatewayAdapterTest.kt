package com.coradio.tgfetch.infrastructure.out.storage

import com.coradio.tgfetch.infrastructure.exception.StorageException
import com.coradio.tgfetch.infrastructure.out.storage.config.StorageProperties
import io.minio.MinioClient
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class MinioStorageGatewayAdapterTest {

    @Mock
    lateinit var minioClient: MinioClient

    @Mock
    lateinit var storageProperties: StorageProperties

    @InjectMocks
    lateinit var adapter: MinioStorageGatewayAdapter

    private val bucket = "radio"

    @BeforeEach
    fun setup() {
        whenever(storageProperties.bucket).thenReturn(bucket)
    }

    @Test
    fun `should upload file successfully`() {

        val path = Files.createTempFile("test", ".mp3")

        adapter.upload("key", path)

        verify(minioClient).putObject(any())
    }

    @Test
    fun `should throw StorageException when upload fails`() {

        val path = Files.createTempFile("test", ".mp3")

        whenever(minioClient.putObject(any()))
            .thenThrow(RuntimeException("boom"))

        val ex = assertThrows<StorageException> {
            adapter.upload("key", path)
        }

        assertTrue(ex.message!!.contains("Failed to upload object"))
        assertNotNull(ex.cause)
    }

    @Test
    fun `should return true when object exists`() {

        whenever(minioClient.statObject(any()))
            .thenReturn(mock())

        val result = adapter.exists("key")

        assertTrue(result)
    }

    @Test
    fun `should return false when statObject throws exception`() {

        whenever(minioClient.statObject(any()))
            .thenThrow(RuntimeException("not found"))

        val result = adapter.exists("key")

        assertFalse(result)
    }

    @Test
    fun `should delete object successfully`() {

        adapter.delete("key")

        verify(minioClient).removeObject(any())
    }

    @Test
    fun `should ignore exception when delete fails`() {

        whenever(minioClient.removeObject(any()))
            .thenThrow(RuntimeException("fail"))

        adapter.delete("key")

        verify(minioClient).removeObject(any())
    }


}
