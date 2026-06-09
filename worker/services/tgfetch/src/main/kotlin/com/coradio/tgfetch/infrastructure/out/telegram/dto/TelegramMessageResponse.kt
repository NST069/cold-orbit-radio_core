package com.coradio.tgfetch.infrastructure.out.telegram.dto

data class TelegramMessageResponse(
    val channelId: Long,
    val messageId: Long,
    val date: Long,

    val type: String,

    val text: String?,

    val audio: AudioResponse?,

    val cover: CoverResponse?,

    val caption: String?
)
