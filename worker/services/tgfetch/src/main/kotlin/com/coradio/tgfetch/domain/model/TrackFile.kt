package com.coradio.tgfetch.domain.model

import java.util.UUID

data class TrackFile(
    val id: UUID? = null,
    val track: Track,
    val sha256: String,
    val telegramFileUniqueId: String,
    val storageKey: String? = null,
    val fileSize: Long,
    val mimeType: String,
)
