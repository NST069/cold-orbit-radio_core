package com.coradio.tgfetch.infrastructure.out.telegram.mapper

import com.coradio.tgfetch.domain.port.out.telegram.MessagePageData
import com.coradio.tgfetch.infrastructure.out.telegram.dto.MessagePageResponse

object MessagePageMapper {
    fun toDomain(messagePageResponse: MessagePageResponse): MessagePageData = MessagePageData(
        items = messagePageResponse.items.map { TelegramMessageMapper.toDomain(it) },
        nextCursor = messagePageResponse.nextCursor,
        hasMore = messagePageResponse.hasMore
    )
}
