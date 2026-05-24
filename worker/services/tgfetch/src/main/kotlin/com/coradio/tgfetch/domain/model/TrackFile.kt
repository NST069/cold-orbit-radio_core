package com.coradio.tgfetch.domain.model

import com.coradio.tgfetch.domain.enums.TrackFileStatus
import java.time.Instant
import java.util.UUID

data class TrackFile(
    val id: UUID? = null,
    val track: Track,
    val etag: String,
    val telegramFileId: String,
    val telegramFileUniqueId: String,
    val storageKey: String? = null,
    val fileSize: Long,
    val mimeType: String,
    val status: TrackFileStatus,
    val retryCount: Int,
    val lastDownloadAttemptAt: Instant
)
