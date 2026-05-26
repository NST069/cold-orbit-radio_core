package com.coradio.tgfetch.infrastructure.telegram

import com.coradio.tgfetch.domain.port.out.telegram.TelegramGatewayPort
import com.coradio.tgfetch.domain.port.out.telegram.TelegramTrackData
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class TelegramGatewayAdapter: TelegramGatewayPort {
    override suspend fun getTrackPosts(): List<TelegramTrackData> {
        TODO("Not yet implemented")
    }

    override suspend fun downloadFile(fileId: String): Path {
        TODO("Not yet implemented")
    }
}
