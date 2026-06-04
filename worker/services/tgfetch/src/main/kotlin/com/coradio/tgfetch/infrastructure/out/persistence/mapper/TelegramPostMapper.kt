package com.coradio.tgfetch.infrastructure.out.persistence.mapper

import com.coradio.tgfetch.domain.model.TelegramPost
import com.coradio.tgfetch.domain.model.view.TelegramPostView
import com.coradio.tgfetch.infrastructure.out.persistence.entity.TelegramPostEntity

object TelegramPostMapper {
    fun toDomain(telegramPostEntity: TelegramPostEntity): TelegramPost = TelegramPost(
        id = telegramPostEntity.id,
        channelId = telegramPostEntity.channelId,
        messageId = telegramPostEntity.messageId,
        rawText = telegramPostEntity.rawText,
        publishedAt = telegramPostEntity.publishedAt,
    )

    fun toTelegramPostView(telegramPostEntity: TelegramPostEntity): TelegramPostView = TelegramPostView(
        id = telegramPostEntity.id,
        trackId = telegramPostEntity.trackEntity.id,
    )

    fun toEntity(telegramPost: TelegramPost): TelegramPostEntity = TelegramPostEntity().apply {
        this.id = telegramPost.id
        this.channelId = telegramPost.channelId
        this.messageId = telegramPost.messageId
        this.rawText = telegramPost.rawText
        this.publishedAt = telegramPost.publishedAt
    }
}
