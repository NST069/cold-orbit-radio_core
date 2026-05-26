package com.coradio.tgfetch.domain.port.out.telegram

import java.time.Instant

data class TelegramTrackData(
    val channelId: Long,
    val messageId: Long,

    val fileId: String,
    val fileUniqueId: String,

    val artist: String?,
    val title: String?,

    val rawText: String?,

    val durationSeconds: Int?,
    val fileSizeBytes: Long?,
    val fileName: String?,
    val mimeType: String?,

    val publishedAt: Instant
)
