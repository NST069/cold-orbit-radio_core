package com.coradio.tgfetch.domain.port.out.telegram

import java.nio.file.Path

interface TelegramGatewayPort {

    suspend fun getTrackPosts(): List<TelegramTrackData>

    suspend fun downloadFile(
        fileId: String
    ): Path
}
