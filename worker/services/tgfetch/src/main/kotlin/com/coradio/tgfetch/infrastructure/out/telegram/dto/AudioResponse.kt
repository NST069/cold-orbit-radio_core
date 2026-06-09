package com.coradio.tgfetch.infrastructure.out.telegram.dto

data class AudioResponse(
    val title: String?,
    val performer: String?,
    val durationSeconds: Int?,
    val fileSizeBytes: Long?,

    val fileName: String?,
    val mimeType: String?,

    val tdFileId: Long?,
    val remoteFileId: String?,
    val uniqueFileId: String?
)
