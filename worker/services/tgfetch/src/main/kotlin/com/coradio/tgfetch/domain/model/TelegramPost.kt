package com.coradio.tgfetch.domain.model

import java.time.Instant
import java.util.UUID

data class TelegramPost(
    val id: UUID? = null,
    val channelId: Long,
    val messageId: Long,
    val track: Track,
    val trackFile: TrackFile,
    val rawText: String? = null,
    val publishedAt: Instant,
)
