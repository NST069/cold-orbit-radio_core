package com.coradio.tgfetch.infrastructure.out.telegram.dto

data class ChannelInfoResponse(
    val id: Long,
    val title: String,
    val username: String
)
