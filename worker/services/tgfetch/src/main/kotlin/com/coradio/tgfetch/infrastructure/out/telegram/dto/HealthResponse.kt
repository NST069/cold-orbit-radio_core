package com.coradio.tgfetch.infrastructure.out.telegram.dto

data class HealthResponse(
    val status: String,
    val telegram: String,
    val uptimeSeconds: Long
)
