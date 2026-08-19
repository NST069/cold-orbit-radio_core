package com.coradio.tgfetch.domain.port.out.telegram

import com.coradio.tgfetch.infrastructure.out.telegram.dto.ChannelInfoResponse
import com.coradio.tgfetch.infrastructure.out.telegram.dto.HealthResponse

interface TelegramGatewayPort {
    fun getChannel(username: String): ChannelInfoResponse

    fun getMessages(
        channelId: Long,
        limit: Int,
        cursor: Long?
    ): MessagePageData

    fun downloadFile(
        remoteFileId: String,
        extension: String?
    ): DownloadFileResponse

    fun removeFile(
        fileId: String
    )

    fun health(): HealthResponse
}
