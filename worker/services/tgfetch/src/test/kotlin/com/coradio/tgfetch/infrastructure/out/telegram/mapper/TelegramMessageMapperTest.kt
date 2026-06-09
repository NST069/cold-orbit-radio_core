package com.coradio.tgfetch.infrastructure.out.telegram.mapper

import com.coradio.tgfetch.infrastructure.out.telegram.mapper.MockEntities.mockTelegramMessageResponse
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals

class TelegramMessageMapperTest {

    @Test
    fun `toDomain should return valid domain object`() {
        val result = TelegramMessageMapper.toDomain(mockTelegramMessageResponse)

        assertEquals(mockTelegramMessageResponse.channelId, result.channelId)
        assertEquals(mockTelegramMessageResponse.messageId, result.messageId)
        assertEquals(mockTelegramMessageResponse.audio?.tdFileId, result.tdFileId)
        assertEquals(mockTelegramMessageResponse.audio?.remoteFileId, result.remoteFileId)
        assertEquals(mockTelegramMessageResponse.audio?.uniqueFileId, result.uniqueFileId)
        assertEquals(mockTelegramMessageResponse.audio?.performer, result.artist)
        assertEquals(mockTelegramMessageResponse.audio?.title, result.title)
        assertEquals(mockTelegramMessageResponse.text, result.rawText)
        assertEquals(mockTelegramMessageResponse.audio?.durationSeconds, result.durationSeconds)
        assertEquals(mockTelegramMessageResponse.audio?.fileSizeBytes, result.fileSizeBytes)
        assertEquals(mockTelegramMessageResponse.audio?.fileName, result.fileName)
        assertEquals(mockTelegramMessageResponse.audio?.mimeType, result.mimeType)
        assertEquals(mockTelegramMessageResponse.cover?.tdFileId, result.coverTdFileId)
        assertEquals(mockTelegramMessageResponse.cover?.remoteFileId, result.coverRemoteFileId)
        assertEquals(mockTelegramMessageResponse.cover?.uniqueFileId, result.coverUniqueFileId)
        assertEquals(Instant.ofEpochSecond(mockTelegramMessageResponse.date), result.publishedAt)
    }

}
