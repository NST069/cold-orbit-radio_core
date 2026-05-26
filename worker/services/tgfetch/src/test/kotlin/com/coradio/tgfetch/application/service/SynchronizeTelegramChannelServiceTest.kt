package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.application.util.MetadataResolver
import com.coradio.tgfetch.domain.model.TelegramPost
import com.coradio.tgfetch.domain.model.Track
import com.coradio.tgfetch.domain.model.TrackFile
import com.coradio.tgfetch.domain.model.TrackMetadata
import com.coradio.tgfetch.domain.port.out.persistence.TelegramPostRepositoryPort
import com.coradio.tgfetch.domain.port.out.telegram.TelegramGatewayPort
import com.coradio.tgfetch.domain.port.out.telegram.TelegramTrackData
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class SynchronizeTelegramChannelServiceTest {

    @Mock
    lateinit var telegramGateway: TelegramGatewayPort

    @Mock
    lateinit var telegramPostRepository: TelegramPostRepositoryPort

    @Mock
    lateinit var metadataResolver: MetadataResolver

    @InjectMocks
    lateinit var synchronizeTelegramChannelService: SynchronizeTelegramChannelService

    lateinit var track: Track
    lateinit var trackFile: TrackFile
    lateinit var telegramPost: TelegramPost
    lateinit var telegramTrackData: TelegramTrackData
    lateinit var trackMetadata: TrackMetadata

    @BeforeEach
    fun setUp() = runTest {
        telegramTrackData = TelegramTrackData(
            channelId = 1234L,
            messageId = 1234L,
            fileId = "12345",
            fileUniqueId = "12345",
            artist = "John Doe",
            title = "Hello",
            rawText = "John Doe - Hello",
            durationSeconds = 1000,
            fileSizeBytes = 1000,
            fileName = "Hello.flac",
            mimeType = "flac",
            publishedAt = Instant.now().minus(5, ChronoUnit.DAYS),
        )

        track = Track(
            title = telegramTrackData.title ?: "",
            artist = telegramTrackData.artist ?: "",
            duration = telegramTrackData.durationSeconds ?: 0,
        )

        trackFile = TrackFile(
            track = track,
            etag = UUID.randomUUID().toString(),
            telegramFileId = telegramTrackData.fileId ?: "",
            telegramFileUniqueId = telegramTrackData.fileUniqueId ?: "",
            fileSize = telegramTrackData.fileSizeBytes ?: 0,
            mimeType = telegramTrackData.mimeType ?: "",
        )

        telegramPost = TelegramPost(
            channelId = telegramTrackData.channelId,
            messageId = telegramTrackData.messageId,
            track = track,
            trackFile = trackFile,
            rawText = telegramTrackData.rawText ?: "",
            publishedAt = telegramTrackData.publishedAt,
        )

        whenever(telegramGateway.getTrackPosts()).thenReturn(listOf(telegramTrackData))

        trackMetadata = TrackMetadata(
            artist = "John Doe",
            title = "Hello",
        )

    }

    @Test
    fun `execute should save a new post`() = runTest {
        whenever(telegramPostRepository.findByChannelAndMessageId(any(), any())).thenReturn(null)
        whenever(telegramPostRepository.save(any())).thenReturn(telegramPost)
        whenever(metadataResolver.resolve(any())).thenReturn(trackMetadata)

        synchronizeTelegramChannelService.execute()

        val telegramPostCaptor = argumentCaptor<TelegramPost>()

        verify(telegramPostRepository, times(1)).save(telegramPostCaptor.capture())

        val resultPost = telegramPostCaptor.firstValue
        val resultTrack = resultPost.track
        val resultTrackFile = resultPost.trackFile

        assertEquals(telegramPost.channelId, resultPost.channelId)
        assertEquals(telegramPost.messageId, resultPost.messageId)
        assertEquals(telegramPost.rawText, resultPost.rawText)
        assertEquals(telegramPost.publishedAt, resultPost.publishedAt)

        assertEquals(track.title, resultTrack.title)
        assertEquals(track.artist, resultTrack.artist)
        assertEquals(track.duration, resultTrack.duration)

        assertEquals(trackFile.telegramFileId, resultTrackFile.telegramFileId)
        assertEquals(trackFile.telegramFileUniqueId, resultTrackFile.telegramFileUniqueId)
        assertEquals(trackFile.fileSize, resultTrackFile.fileSize)
        assertEquals(trackFile.mimeType, resultTrackFile.mimeType)

        verify(telegramPostRepository, times(1)).findByChannelAndMessageId(any(), any())

    }

    @Test
    fun `execute should do nothing if track exists`() = runTest {
        whenever(telegramPostRepository.findByChannelAndMessageId(any(), any())).thenReturn(telegramPost)

        synchronizeTelegramChannelService.execute()

        verify(telegramPostRepository, times(1)).findByChannelAndMessageId(any(), any())
        verify(telegramPostRepository, never()).save(any())

    }

    @Test
    fun `execute should change track if trackFileUniqueId not matching`() = runTest {
        val existingTrackFile = TrackFile(
            track = trackFile.track,
            etag = trackFile.etag,
            telegramFileId = trackFile.telegramFileId,
            telegramFileUniqueId = "anotherTelegramFileUniqueId",
            fileSize = trackFile.fileSize,
            mimeType = trackFile.mimeType,
        )
        val existingPost = TelegramPost(
            channelId = telegramPost.channelId,
            messageId = telegramPost.messageId,
            track = telegramPost.track,
            trackFile = existingTrackFile,
            rawText = telegramPost.rawText,
            publishedAt = telegramPost.publishedAt,
        )

        whenever(telegramPostRepository.findByChannelAndMessageId(any(), any())).thenReturn(existingPost)
        whenever(telegramPostRepository.save(any())).thenReturn(telegramPost)

        synchronizeTelegramChannelService.execute()

        val telegramPostCaptor = argumentCaptor<TelegramPost>()

        verify(telegramPostRepository, times(1)).save(telegramPostCaptor.capture())

        val resultPost = telegramPostCaptor.firstValue
        val resultTrack = resultPost.track
        val resultTrackFile = resultPost.trackFile

        assertEquals(telegramPost.channelId, resultPost.channelId)
        assertEquals(telegramPost.messageId, resultPost.messageId)
        assertEquals(telegramPost.rawText, resultPost.rawText)
        assertEquals(telegramPost.publishedAt, resultPost.publishedAt)

        assertEquals(track.title, resultTrack.title)
        assertEquals(track.artist, resultTrack.artist)
        assertEquals(track.duration, resultTrack.duration)

        assertEquals(trackFile.telegramFileId, resultTrackFile.telegramFileId)
        assertEquals(trackFile.telegramFileUniqueId, resultTrackFile.telegramFileUniqueId)
        assertEquals(trackFile.fileSize, resultTrackFile.fileSize)
        assertEquals(trackFile.mimeType, resultTrackFile.mimeType)

        verify(telegramPostRepository, times(1)).findByChannelAndMessageId(any(), any())

    }

}
