package com.coradio.tgfetch.domain.port.out.telegram

data class MessagePageData(
    val items: List<TelegramTrackData>,
    val nextCursor: Long?,
    val hasMore: Boolean
)
