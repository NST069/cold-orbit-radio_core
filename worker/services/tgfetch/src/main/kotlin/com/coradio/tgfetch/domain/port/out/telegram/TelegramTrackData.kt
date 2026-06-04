package com.coradio.tgfetch.domain.port.out.telegram

import java.time.Instant

data class TelegramTrackData(
    val channelId: Long,
    val messageId: Long,

    val tdFileId: Long,
	val remoteFileId: String,
    val fileUniqueId: String,

    val artist: String?,
    val title: String?,

    val rawText: String?,

    val durationSeconds: Int?,
    val fileSizeBytes: Long?,
    val fileName: String?,
    val mimeType: String?,

    val coverTdFileId: Long?,
	val coverRemoteFileId: String?,
    val coverUniqueFileId: String?,

    val publishedAt: Instant
)
