package com.coradio.tgfetch.infrastructure.out.telegram.dto

data class MessagePageResponse(
    val items: List<TelegramMessageResponse>,
    val nextCursor: Long?,
    val hasMore: Boolean
)
