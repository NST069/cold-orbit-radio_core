package com.coradio.tgfetch.infrastructure.out.telegram.mapper

import com.coradio.tgfetch.domain.port.out.telegram.TelegramTrackData
import com.coradio.tgfetch.infrastructure.out.telegram.dto.TelegramMessageResponse
import java.time.Instant

object TelegramMessageMapper {
    fun toDomain(telegramMessageResponse: TelegramMessageResponse): TelegramTrackData {
        val audio = telegramMessageResponse.audio
        val cover = telegramMessageResponse.cover
        return TelegramTrackData(
            channelId = telegramMessageResponse.channelId,
            messageId = telegramMessageResponse.messageId,

            tdFileId = audio?.tdFileId ?: 0,
            remoteFileId = audio?.remoteFileId ?: "",
            uniqueFileId = audio?.uniqueFileId ?: "",

            artist = audio?.performer,
            title = audio?.title,

            rawText = telegramMessageResponse.text,

            durationSeconds = audio?.durationSeconds,
            fileSizeBytes = audio?.fileSizeBytes,
            fileName = audio?.fileName,
            mimeType = audio?.mimeType,

            coverTdFileId = cover?.tdFileId,
            coverRemoteFileId = cover?.remoteFileId,
            coverUniqueFileId = cover?.uniqueFileId,

            publishedAt = Instant.ofEpochSecond(telegramMessageResponse.date)
        )
    }
}
