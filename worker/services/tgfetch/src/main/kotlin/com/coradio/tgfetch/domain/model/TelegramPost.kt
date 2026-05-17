package com.coradio.tgfetch.domain.model

import java.util.UUID

data class TelegramPost(
    val id: UUID? = null,
    val telegramPostId: String,
    val track: Track,
    val trackFile: TrackFile,
    val rawText: String? = null,
)
