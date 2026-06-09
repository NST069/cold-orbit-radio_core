package com.coradio.tgfetch.infrastructure.out.persistence.repository

import com.coradio.tgfetch.infrastructure.out.persistence.entity.TelegramPostEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TelegramPostRepository : JpaRepository<TelegramPostEntity, UUID> {

    fun findByChannelIdAndMessageId(
        channelId: Long,
        messageId: Long
    ): TelegramPostEntity?

    fun existsByChannelIdAndMessageId(channelId: Long, messageId: Long): Boolean

}
