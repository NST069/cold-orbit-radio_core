package com.coradio.tgfetch.infrastructure.out.telegram

import com.coradio.tgfetch.infrastructure.exception.TelegramException
import com.coradio.tgfetch.infrastructure.out.telegram.dto.ChannelInfoResponse
import com.coradio.tgfetch.infrastructure.out.telegram.dto.HealthResponse
import com.coradio.tgfetch.infrastructure.out.telegram.dto.MessagePageResponse
import com.coradio.tgfetch.infrastructure.out.telegram.dto.MessagesResponse
import com.coradio.tgfetch.infrastructure.out.telegram.port.TempFileStorage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClient
import java.io.InputStream
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFalse

@ExtendWith(MockitoExtension::class)
class TelegramGatewayAdapterTest {

    @Mock
    lateinit var restClient: RestClient

    @Mock
    lateinit var tempStorage: TempFileStorage

    @InjectMocks
    lateinit var adapter: TelegramGatewayAdapter

    @Test
    fun `getChannel should return channel info`() {

        val request = mock<RestClient.RequestHeadersUriSpec<*>>()
        val headers = mock<RestClient.RequestHeadersSpec<*>>()
        val response = mock<RestClient.ResponseSpec>()

        val body = ChannelInfoResponse(
            id = 1,
            title = "Channel",
            username = "channel"
        )

        whenever(restClient.get()).thenReturn(request)
        whenever(request.uri("/channels/by-username/{username}", "channel"))
            .thenReturn(headers)
        whenever(headers.retrieve()).thenReturn(response)
        whenever(response.body(ChannelInfoResponse::class.java))
            .thenReturn(body)

        val result = adapter.getChannel("channel")

        assertEquals(body, result)
    }

    @Test
    fun `getChannel should throw when body is null`() {

        val request = mock<RestClient.RequestHeadersUriSpec<*>>()
        val headers = mock<RestClient.RequestHeadersSpec<*>>()
        val response = mock<RestClient.ResponseSpec>()

        whenever(restClient.get()).thenReturn(request)
        whenever(request.uri("/channels/by-username/{username}", "channel"))
            .thenReturn(headers)
        whenever(headers.retrieve()).thenReturn(response)
        whenever(response.body(ChannelInfoResponse::class.java))
            .thenReturn(null)

        assertThrows<TelegramException> {
            adapter.getChannel("channel")
        }
    }

    @Test
    fun `getMessages should return mapped page`() {

        val request = mock<RestClient.RequestHeadersUriSpec<*>>()
        val headers = mock<RestClient.RequestHeadersSpec<*>>()
        val response = mock<RestClient.ResponseSpec>()

        val body = MessagesResponse(
            MessagePageResponse(
                items = emptyList(),
                nextCursor = 100L,
                hasMore = true
            )
        )

        whenever(restClient.get()).thenReturn(request)

        doReturn(headers)
            .whenever(request)
            .uri(any<java.util.function.Function<org.springframework.web.util.UriBuilder, java.net.URI>>())

        whenever(headers.retrieve()).thenReturn(response)

        whenever(response.body(MessagesResponse::class.java))
            .thenReturn(body)

        val result = adapter.getMessages(
            channelId = 1L,
            limit = 50,
            cursor = 10L
        )

        assertTrue(result.hasMore)
        assertEquals(100L, result.nextCursor)
    }

    @Test
    fun `getMessages should throw when body is null`() {

        val request = mock<RestClient.RequestHeadersUriSpec<*>>()
        val headers = mock<RestClient.RequestHeadersSpec<*>>()
        val response = mock<RestClient.ResponseSpec>()

        whenever(restClient.get()).thenReturn(request)

        doReturn(headers)
            .whenever(request)
            .uri(any<java.util.function.Function<org.springframework.web.util.UriBuilder, java.net.URI>>())

        whenever(headers.retrieve()).thenReturn(response)

        whenever(response.body(MessagesResponse::class.java))
            .thenReturn(null)

        assertThrows<TelegramException> {
            adapter.getMessages(
                channelId = 1L,
                limit = 50,
                cursor = null
            )
        }
    }

    @Test
    fun `downloadFile should save file`() {

        val file = Files.createTempFile("telegram", ".tmp")

        whenever(
            tempStorage.createTempFile(
                "remote-id",
                "mp3"
            )
        ).thenReturn(file)

        val request = mock<RestClient.RequestHeadersUriSpec<*>>()
        val headers = mock<RestClient.RequestHeadersSpec<*>>()
        val response = mock<RestClient.ResponseSpec>()

        whenever(restClient.get()).thenReturn(request)

        whenever(
            request.uri(
                "/files/by-remote/{remoteFileId}",
                "remote-id"
            )
        ).thenReturn(headers)

        whenever(headers.retrieve()).thenReturn(response)

        val responseHeaders = HttpHeaders()
        responseHeaders.add("X-Telegram-File-Id", "1234")

        whenever(response.toEntity(InputStream::class.java))
            .thenReturn(
                ResponseEntity<InputStream>(
                    "hello".toByteArray().inputStream(),
                    responseHeaders,
                    HttpStatus.OK
                )
            )

        val result = adapter.downloadFile(
            "remote-id",
            "mp3"
        )

        assertTrue(Files.exists(result.path))
        assertTrue(Files.size(result.path) > 0)
    }

    @Test
    fun `downloadFile should throw when response body is null`() {

        val file = Files.createTempFile("telegram", ".tmp")

        whenever(
            tempStorage.createTempFile(
                "remote-id",
                "mp3"
            )
        ).thenReturn(file)

        val request = mock<RestClient.RequestHeadersUriSpec<*>>()
        val headers = mock<RestClient.RequestHeadersSpec<*>>()
        val response = mock<RestClient.ResponseSpec>()

        whenever(restClient.get()).thenReturn(request)

        whenever(
            request.uri(
                "/files/by-remote/{remoteFileId}",
                "remote-id"
            )
        ).thenReturn(headers)

        whenever(headers.retrieve()).thenReturn(response)

        assertThrows<TelegramException> {
            adapter.downloadFile(
                "remote-id",
                "mp3"
            )
        }

        assertFalse(Files.exists(file))
    }

    @Test
    fun `downloadFile should delete temp file on exception`() {

        val file = Files.createTempFile("telegram", ".tmp")

        whenever(
            tempStorage.createTempFile(
                "remote-id",
                "mp3"
            )
        ).thenReturn(file)

        whenever(restClient.get())
            .thenThrow(RuntimeException("boom"))

        assertThrows<TelegramException> {
            adapter.downloadFile(
                "remote-id",
                "mp3"
            )
        }

        assertFalse(Files.exists(file))
    }

    @Test
    fun `deleteFile should remove file from TGate`() {
        val fileId = "1234"

        val request = mock<RestClient.RequestHeadersUriSpec<*>>()
        val headers = mock<RestClient.RequestHeadersSpec<*>>()
        val response = mock<RestClient.ResponseSpec>()

        whenever(restClient.delete()).thenReturn(request)

        whenever(
            request.uri(
                "/files/{fileId}",
                fileId
            )
        ).thenReturn(headers)

        whenever(headers.retrieve()).thenReturn(response)

        whenever(response.toBodilessEntity())
            .thenReturn(ResponseEntity.noContent().build())

        adapter.removeFile(fileId)

        verify(restClient).delete()

        verify(request).uri(
            "/files/{fileId}",
            fileId
        )

        verify(headers).retrieve()

        verify(response).toBodilessEntity()
    }

    @Test
    fun `deleteFile should not throw when TGate fails`() {
        val fileId = "1234"

        val request = mock<RestClient.RequestHeadersUriSpec<*>>()
        val headers = mock<RestClient.RequestHeadersSpec<*>>()
        val response = mock<RestClient.ResponseSpec>()

        whenever(restClient.delete()).thenReturn(request)

        whenever(
            request.uri(
                "/files/{fileId}",
                fileId
            )
        ).thenReturn(headers)

        whenever(headers.retrieve()).thenReturn(response)

        whenever(response.toBodilessEntity())
            .thenThrow(RuntimeException("TGate unavailable"))

        assertDoesNotThrow {
            adapter.removeFile(fileId)
        }
    }

    @Test
    fun `health should return response`() {

        val request = mock<RestClient.RequestHeadersUriSpec<*>>()
        val headers = mock<RestClient.RequestHeadersSpec<*>>()
        val response = mock<RestClient.ResponseSpec>()

        val body = HealthResponse(
            status = "UP",
            telegram = "UP",
            uptimeSeconds = 123
        )

        whenever(restClient.get()).thenReturn(request)
        whenever(request.uri("/health"))
            .thenReturn(headers)
        whenever(headers.retrieve())
            .thenReturn(response)
        whenever(response.body(HealthResponse::class.java))
            .thenReturn(body)

        val result = adapter.health()

        assertEquals(body, result)
    }

    @Test
    fun `health should throw when body is null`() {

        val request = mock<RestClient.RequestHeadersUriSpec<*>>()
        val headers = mock<RestClient.RequestHeadersSpec<*>>()
        val response = mock<RestClient.ResponseSpec>()

        whenever(restClient.get()).thenReturn(request)
        whenever(request.uri("/health"))
            .thenReturn(headers)
        whenever(headers.retrieve())
            .thenReturn(response)
        whenever(response.body(HealthResponse::class.java))
            .thenReturn(null)

        assertThrows<TelegramException> {
            adapter.health()
        }
    }
}
