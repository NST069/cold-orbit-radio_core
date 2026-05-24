package com.coradio.tgfetch.infrastructure.persistence.mapper

import com.coradio.tgfetch.domain.model.TelegramPost
import com.coradio.tgfetch.infrastructure.persistence.entity.TelegramPostEntity

object TelegramPostMapper {
    fun toDomain(telegramPostEntity: TelegramPostEntity): TelegramPost = TelegramPost(
        id = telegramPostEntity.id,
        channelId = telegramPostEntity.channelId,
        messageId = telegramPostEntity.messageId,
        track = TrackMapper.toDomain(telegramPostEntity.trackEntity),
        trackFile = TrackFileMapper.toDomain(telegramPostEntity.trackFileEntity),
        rawText = telegramPostEntity.rawText,
        publishedAt = telegramPostEntity.publishedAt,
    )

    fun toEntity(telegramPost: TelegramPost): TelegramPostEntity = TelegramPostEntity().apply {
        this.id = telegramPost.id
        this.channelId = telegramPost.channelId
        this.messageId = telegramPost.messageId
        this.trackEntity = TrackMapper.toEntity(telegramPost.track)
        this.trackFileEntity = TrackFileMapper.toEntity(telegramPost.trackFile)
        this.rawText = telegramPost.rawText
        this.publishedAt = telegramPost.publishedAt
    }
}
