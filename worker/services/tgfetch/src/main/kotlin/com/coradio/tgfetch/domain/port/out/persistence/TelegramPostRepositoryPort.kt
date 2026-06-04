package com.coradio.tgfetch.domain.port.out.persistence

import com.coradio.tgfetch.domain.model.TelegramPost
import com.coradio.tgfetch.domain.model.view.TelegramPostView
import java.util.UUID

interface TelegramPostRepositoryPort {
    fun save(telegramPost: TelegramPost): TelegramPost
    fun findById(id: UUID): TelegramPost?
    fun findAll(): List<TelegramPost>
    fun deleteById(id: UUID)

    fun findByChannelAndMessageId(
        channelId: Long,
        messageId: Long
    ): TelegramPostView?

    fun existsByChannelAndMessageId(
        channelId: Long,
        messageId: Long
    ): Boolean

    fun saveAll(newMessages: List<TelegramPost>)
}
