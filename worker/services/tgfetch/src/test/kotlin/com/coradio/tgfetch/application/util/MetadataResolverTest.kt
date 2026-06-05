package com.coradio.tgfetch.application.util

import com.coradio.tgfetch.domain.model.valueobject.TrackMetadata
import com.coradio.tgfetch.domain.port.out.telegram.TelegramTrackData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class MetadataResolverTest {

    lateinit var resolver: MetadataResolver

    lateinit var telegramTrackData: TelegramTrackData

    lateinit var trackMetadata: TrackMetadata

    @BeforeEach
    fun setUp() {
        resolver = MetadataResolver()

        trackMetadata = TrackMetadata(
            artist = "John Doe",
            title = "Hello",
        )
    }

    @Test
    fun `resolve from caption`() {
        telegramTrackData = TelegramTrackData(
            channelId = 1234L,
            messageId = 1234L,
            tdFileId = 12345L,
            remoteFileId = "12345",
            uniqueFileId = "12345",
            artist = "John Doe",
            title = "Hello",
            rawText = "John Doe - Hello",
            durationSeconds = 1000,
            fileSizeBytes = 1000,
            fileName = "Hello.flac",
            mimeType = "flac",
            coverTdFileId = 0,
            coverRemoteFileId = null,
            coverUniqueFileId = null,
            publishedAt = Instant.now().minus(5, ChronoUnit.DAYS),
        )

        val result = resolver.resolve(telegramTrackData)

        assertEquals(trackMetadata.artist, result.artist)
        assertEquals(trackMetadata.title, result.title)

    }

    @Test
    fun `resolve from track metadata`() {
        telegramTrackData = TelegramTrackData(
            channelId = 1234L,
            messageId = 1234L,
            tdFileId = 12345L,
            remoteFileId = "12345",
            uniqueFileId = "12345",
            artist = "John Doe",
            title = "Hello",
            rawText = "",
            durationSeconds = 1000,
            fileSizeBytes = 1000,
            fileName = "Hello.flac",
            mimeType = "flac",
            coverTdFileId = 0,
            coverRemoteFileId = null,
            coverUniqueFileId = null,
            publishedAt = Instant.now().minus(5, ChronoUnit.DAYS),
        )

        val result = resolver.resolve(telegramTrackData)

        assertEquals(trackMetadata.artist, result.artist)
        assertEquals(trackMetadata.title, result.title)

    }

    @Test
    fun `resolve from metadata empty field`() {
        telegramTrackData = TelegramTrackData(
            channelId = 1234L,
            messageId = 1234L,
            tdFileId = 12345L,
            remoteFileId = "12345",
            uniqueFileId = "12345",
            artist = "",
            title = "Hello",
            rawText = "",
            durationSeconds = 1000,
            fileSizeBytes = 1000,
            fileName = "Hello.flac",
            mimeType = "flac",
            coverTdFileId = 0,
            coverRemoteFileId = null,
            coverUniqueFileId = null,
            publishedAt = Instant.now().minus(5, ChronoUnit.DAYS),
        )

        val result = resolver.resolve(telegramTrackData)

        assertEquals("<Неизвестен>", result.artist)
        assertEquals(trackMetadata.title, result.title)

    }

    @Test
    fun `resolve from file name`() {
        telegramTrackData = TelegramTrackData(
            channelId = 1234L,
            messageId = 1234L,
            tdFileId = 12345L,
            remoteFileId = "12345",
            uniqueFileId = "12345",
            artist = "",
            title = "",
            rawText = "",
            durationSeconds = 1000,
            fileSizeBytes = 1000,
            fileName = "John Doe - Hello.flac",
            mimeType = "flac",
            coverTdFileId = 0,
            coverRemoteFileId = null,
            coverUniqueFileId = null,
            publishedAt = Instant.now().minus(5, ChronoUnit.DAYS),
        )

        val result = resolver.resolve(telegramTrackData)

        assertEquals(trackMetadata.artist, result.artist)
        assertEquals(trackMetadata.title, result.title)

    }


    @Test
    fun `resolve failed return track unrecognized`() {
        telegramTrackData = TelegramTrackData(
            channelId = 1234L,
            messageId = 1234L,
            tdFileId = 12345L,
            remoteFileId = "12345",
            uniqueFileId = "12345",
            artist = "",
            title = "",
            rawText = "",
            durationSeconds = 1000,
            fileSizeBytes = 1000,
            fileName = "Hello.flac",
            mimeType = "flac",
            coverTdFileId = 0,
            coverRemoteFileId = null,
            coverUniqueFileId = null,
            publishedAt = Instant.now().minus(5, ChronoUnit.DAYS),
        )

        val result = resolver.resolve(telegramTrackData)

        assertEquals("<Неизвестен>", result.artist)
        assertEquals("<Трек не распознан>", result.title)

    }

}
