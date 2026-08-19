package com.coradio.tgfetch.infrastructure.out.telegram

import com.coradio.tgfetch.domain.port.out.telegram.MessagePageData
import com.coradio.tgfetch.domain.port.out.telegram.TelegramGatewayPort
import com.coradio.tgfetch.infrastructure.exception.TelegramException
import com.coradio.tgfetch.infrastructure.out.telegram.dto.ChannelInfoResponse
import com.coradio.tgfetch.domain.port.out.telegram.DownloadFileResponse
import com.coradio.tgfetch.infrastructure.out.telegram.dto.HealthResponse
import com.coradio.tgfetch.infrastructure.out.telegram.dto.MessagesResponse
import com.coradio.tgfetch.infrastructure.out.telegram.mapper.MessagePageMapper
import com.coradio.tgfetch.infrastructure.out.telegram.port.TempFileStorage
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Service
class TelegramGatewayAdapter(
    private val restClient: RestClient,
    private val tempStorage: TempFileStorage
): TelegramGatewayPort {

    private val log = logger {}

    override fun getChannel(username: String): ChannelInfoResponse =
        restClient.get()
            .uri("/channels/by-username/{username}", username)
            .retrieve()
            .body(ChannelInfoResponse::class.java)
            ?: throw TelegramException("Channel not found")

    override fun getMessages(
        channelId: Long,
        limit: Int,
        cursor: Long?
    ): MessagePageData {
        log.debug { "Getting at most $limit messages for $channelId from $cursor" }
        val response = restClient.get()
            .uri { builder ->

                builder
                    .path("/messages/{channelId}")
                    .queryParam("limit", limit)

                cursor?.let {
                    builder.queryParam("fromMessageId", it)
                }

                builder.build(channelId)
            }
            .retrieve()
            .body(MessagesResponse::class.java)
            ?: throw TelegramException("Messages not found")

        return MessagePageMapper.toDomain(response.messages)
    }

    override fun downloadFile(remoteFileId: String, extension: String?): DownloadFileResponse {
        val safeExt = extension?.takeIf { it.isNotBlank() } ?: "tmp"
        val file = tempStorage.createTempFile(remoteFileId, safeExt)
        log.debug { "Downloading $remoteFileId -> .$safeExt" }

        try {
            val response = restClient.get()
                .uri("/files/by-remote/{remoteFileId}", remoteFileId)
                .retrieve()
                .toEntity(InputStream::class.java)

            response.body
                ?.use { input ->
                    Files.copy(input, file, StandardCopyOption.REPLACE_EXISTING)
                } ?: throw TelegramException("File download: Empty response $remoteFileId")

            val fileId = response.headers.getFirst("X-Telegram-File-Id")
                ?: throw TelegramException( "File download: Missing X-Telegram-File-Id header $remoteFileId")

            log.debug { "File downloaded to ${file.fileName}" }
            return DownloadFileResponse(file, fileId)
        } catch (ex: Exception) {
            log.warn(ex) { "Failed to download file $remoteFileId" }
            Files.deleteIfExists(file)
            throw TelegramException("Failed to download file $remoteFileId", ex)
        }
    }

    override fun removeFile(fileId: String) {
        try {
            restClient.delete()
                .uri("/files/{fileId}", fileId)
                .retrieve()
                .toBodilessEntity()
            log.debug { "Removed file: $fileId" }
        } catch (ex: Exception) {
            log.warn(ex) { "Failed to remove file $fileId" }
        }

    }

    override fun health(): HealthResponse =
        restClient.get()
            .uri("/health")
            .retrieve()
            .body(HealthResponse::class.java)
            ?: throw TelegramException("Health check: Empty response")

}
