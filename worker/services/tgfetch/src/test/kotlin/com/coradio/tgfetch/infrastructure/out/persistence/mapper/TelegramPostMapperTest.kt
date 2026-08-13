package com.coradio.tgfetch.infrastructure.out.persistence.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TelegramPostMapperTest {

    @Test
    fun `toDomain should return valid domain object`() {
        val result = TelegramPostMapper.toDomain(MockEntities.mockTelegramPostEntity)

        assertEquals(MockEntities.mockTelegramPost.id, result.id)
        assertEquals(MockEntities.mockTelegramPost.channelId, result.channelId)
        assertEquals(MockEntities.mockTelegramPost.messageId, result.messageId)
        assertEquals(MockEntities.mockTelegramPost.rawText, result.rawText)
        assertEquals(MockEntities.mockTelegramPost.publishedAt, result.publishedAt)
    }

    @Test
    fun `toEntity should return valid entity`() {
        val result = TelegramPostMapper.toEntity(MockEntities.mockTelegramPost)

        assertEquals(MockEntities.mockTelegramPostEntity.id, result.id)
        assertEquals(MockEntities.mockTelegramPostEntity.channelId, result.channelId)
        assertEquals(MockEntities.mockTelegramPostEntity.messageId, result.messageId)
        assertEquals(MockEntities.mockTelegramPostEntity.rawText, result.rawText)
        assertEquals(MockEntities.mockTelegramPostEntity.publishedAt, result.publishedAt)
    }

}
