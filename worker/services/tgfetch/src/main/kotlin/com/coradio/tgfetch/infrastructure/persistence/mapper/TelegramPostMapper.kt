package com.coradio.tgfetch.infrastructure.persistence.mapper

import com.coradio.tgfetch.domain.model.TelegramPost
import com.coradio.tgfetch.infrastructure.persistence.entity.TelegramPostEntity

object TelegramPostMapper {
    fun toDomain(telegramPostEntity: TelegramPostEntity): TelegramPost = TelegramPost(
        id = telegramPostEntity.id,
        telegramPostId = telegramPostEntity.telegramPostId,
        track = TrackMapper.toDomain(telegramPostEntity.trackEntity),
        trackFile = TrackFileMapper.toDomain(telegramPostEntity.trackFileEntity),
        rawText = telegramPostEntity.rawText
    )

    fun toEntity(telegramPost: TelegramPost): TelegramPostEntity = TelegramPostEntity().apply {
        this.id = telegramPost.id
        this.telegramPostId = telegramPost.telegramPostId
        this.trackEntity = TrackMapper.toEntity(telegramPost.track)
        this.trackFileEntity = TrackFileMapper.toEntity(telegramPost.trackFile)
        this.rawText = telegramPost.rawText
    }
}
