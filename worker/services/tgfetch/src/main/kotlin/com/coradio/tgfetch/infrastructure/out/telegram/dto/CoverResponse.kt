package com.coradio.tgfetch.infrastructure.out.telegram.dto

data class CoverResponse(
    val tdFileId: Long?,
    val remoteFileId: String?,
    val uniqueFileId: String?
)
