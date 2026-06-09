package com.coradio.tgfetch.infrastructure.out.telegram.mapper

import com.coradio.tgfetch.infrastructure.out.telegram.dto.AudioResponse
import com.coradio.tgfetch.infrastructure.out.telegram.dto.CoverResponse
import com.coradio.tgfetch.infrastructure.out.telegram.dto.MessagePageResponse
import com.coradio.tgfetch.infrastructure.out.telegram.dto.TelegramMessageResponse

object MockEntities {

    val mockTelegramAudio = AudioResponse(
        title = "title",
        performer = "author",
        durationSeconds = 100,
        fileSizeBytes = 100L,
        fileName = "author - title.mp3",
        mimeType = "audio/mpeg",
        tdFileId = 1234L,
        remoteFileId = "1234",
        uniqueFileId = "1234",
    )

    val mockTelegramCover = CoverResponse(
        tdFileId = 1234L,
        remoteFileId = "1234",
        uniqueFileId = "1234",
    )

    val mockTelegramMessageResponse = TelegramMessageResponse(
        channelId = 1234L,
        messageId = 1234L,
        date = 1234L,
        type = "audio",
        text = "Hello World",
        audio = mockTelegramAudio,
        cover = mockTelegramCover,
        caption = "artist - title",
    )

    val mockMessagePageResponse = MessagePageResponse(
        items = listOf(mockTelegramMessageResponse),
        nextCursor = 0,
        hasMore = true,
    )
}
