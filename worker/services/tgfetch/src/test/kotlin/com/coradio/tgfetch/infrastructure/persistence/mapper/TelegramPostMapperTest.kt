package com.coradio.tgfetch.infrastructure.persistence.mapper

import com.coradio.tgfetch.infrastructure.persistence.mapper.MockEntities.mockTelegramPostEntity
import com.coradio.tgfetch.infrastructure.persistence.mapper.MockEntities.mockTelegramPost
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TelegramPostMapperTest {

    @Test
    fun `toDomain should return valid domain object`() {
        val result = TelegramPostMapper.toDomain(mockTelegramPostEntity)

        assertEquals(mockTelegramPost.id, result.id)
        assertEquals(mockTelegramPost.channelId, result.channelId)
        assertEquals(mockTelegramPost.messageId, result.messageId)
        assertEquals(mockTelegramPost.track.id, result.track.id)
        assertEquals(mockTelegramPost.trackFile.id, result.trackFile.id)
        assertEquals(mockTelegramPost.rawText, result.rawText)
        assertEquals(mockTelegramPost.publishedAt, result.publishedAt)
    }

    @Test
    fun `toEntity should return valid entity`() {
        val result = TelegramPostMapper.toEntity(mockTelegramPost)

        assertEquals(mockTelegramPostEntity.id, result.id)
        assertEquals(mockTelegramPost.channelId, result.channelId)
        assertEquals(mockTelegramPostEntity.messageId, result.messageId)
        assertEquals(mockTelegramPostEntity.trackEntity.id, result.trackEntity.id)
        assertEquals(mockTelegramPostEntity.trackFileEntity.id, result.trackFileEntity.id)
        assertEquals(mockTelegramPostEntity.rawText, result.rawText)
        assertEquals(mockTelegramPostEntity.publishedAt, result.publishedAt)
    }

}
