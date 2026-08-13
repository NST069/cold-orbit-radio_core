package com.coradio.tgfetch.domain.model.view

import java.util.UUID

data class TrackFileJobView(
    val id: UUID? = null,
    val telegramFileId: String,
    val artist: String,
    val title: String,
    val retryCount: Int,
    val mimeType: String? = null,
    val fileName: String? = null,
)
