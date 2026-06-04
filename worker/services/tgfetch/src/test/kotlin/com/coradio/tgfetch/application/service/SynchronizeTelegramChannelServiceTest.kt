package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.application.util.MetadataResolver
import com.coradio.tgfetch.domain.model.TelegramPost
import com.coradio.tgfetch.domain.model.Track
import com.coradio.tgfetch.domain.model.TrackFile
import com.coradio.tgfetch.domain.model.valueobject.TrackMetadata
import com.coradio.tgfetch.domain.model.view.TelegramPostView
import com.coradio.tgfetch.domain.port.out.persistence.TelegramPostRepositoryPort
import com.coradio.tgfetch.domain.port.out.persistence.TrackRepositoryPort
import com.coradio.tgfetch.domain.port.out.telegram.MessagePageData
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
    lateinit var trackRepository: TrackRepositoryPort

    @Mock
    lateinit var metadataResolver: MetadataResolver

    @InjectMocks
    lateinit var synchronizeTelegramChannelService: SynchronizeTelegramChannelService

    lateinit var track: Track
    lateinit var trackFile: TrackFile
    lateinit var telegramPost: TelegramPost
    lateinit var telegramTrackData: TelegramTrackData
    lateinit var trackMetadata: TrackMetadata
    lateinit var messagePageData: MessagePageData
    lateinit var telegramPostView: TelegramPostView

    val channelId = 12345L

    @BeforeEach
    fun setUp() = runTest {
        telegramTrackData = TelegramTrackData(
            channelId = 1234L,
            messageId = 1234L,
            tdFileId = 12345L,
            remoteFileId = "12345",
            fileUniqueId = "12345",
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

        trackFile = TrackFile(
            etag = UUID.randomUUID().toString(),
            telegramFileId = telegramTrackData.remoteFileId,
            telegramFileUniqueId = telegramTrackData.fileUniqueId,
            fileName = telegramTrackData.fileName ?:"",
            fileSize = telegramTrackData.fileSizeBytes ?: 0,
            mimeType = telegramTrackData.mimeType ?: "",
        )

        telegramPost = TelegramPost(
            channelId = telegramTrackData.channelId,
            messageId = telegramTrackData.messageId,
            rawText = telegramTrackData.rawText ?: "",
            publishedAt = telegramTrackData.publishedAt,
        )

        track = Track(
            title = telegramTrackData.title ?: "",
            artist = telegramTrackData.artist ?: "",
            duration = telegramTrackData.durationSeconds ?: 0,
            trackFile = trackFile,
            telegramPost = telegramPost,
        )

        messagePageData = MessagePageData(
            items = listOf(telegramTrackData),
            nextCursor = 0,
            hasMore = false
        )

        telegramPostView = TelegramPostView(
            id = telegramPost.id,
            trackId = telegramPost.trackId,
        )

        whenever(telegramGateway.getMessages(any(), any(), any())).thenReturn(messagePageData)

        trackMetadata = TrackMetadata(
            artist = "John Doe",
            title = "Hello",
        )

    }

    @Test
    fun `execute should save a new post`() = runTest {
        whenever(telegramPostRepository.findByChannelAndMessageId(any(), any())).thenReturn(null)
        whenever(trackRepository.save(any())).thenReturn(track)
        whenever(metadataResolver.resolve(any())).thenReturn(trackMetadata)

        synchronizeTelegramChannelService.execute(channelId)

        val trackCaptor = argumentCaptor<Track>()

        verify(trackRepository, times(1)).save(trackCaptor.capture())

        val resultTrack = trackCaptor.firstValue
        val resultTrackFile = resultTrack.trackFile!!
        val resultPost = resultTrack.telegramPost!!

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
        whenever(telegramPostRepository.findByChannelAndMessageId(any(), any())).thenReturn(telegramPostView)

        synchronizeTelegramChannelService.execute(channelId)

        verify(telegramPostRepository, times(1)).findByChannelAndMessageId(any(), any())
        verify(trackRepository, never()).save(any())

    }

    @Test
    fun `execute should change track if attributes not matching`() = runTest {
        val existingTrackFile = TrackFile(
            etag = trackFile.etag,
            telegramFileId = trackFile.telegramFileId,
            telegramFileUniqueId = "anotherTelegramFileUniqueId",
            fileName = trackFile.fileName,
            fileSize = trackFile.fileSize,
            mimeType = trackFile.mimeType,
        )
        val existingPost = TelegramPost(
            channelId = telegramPost.channelId,
            messageId = telegramPost.messageId,
            trackId = track.id,
            rawText = "artist - title",
            publishedAt = telegramPost.publishedAt,
        )

        val existingTrack = Track(
            id = track.id,
            artist = "artist",
            title = "title",
            duration = track.duration,
            telegramPost = existingPost,
            trackFile = existingTrackFile,
        )

        whenever(telegramPostRepository.findByChannelAndMessageId(any(), any())).thenReturn(telegramPostView)
        //whenever(telegramPostRepository.save(any())).thenReturn(telegramPost)
        whenever(trackRepository.findById(any())).thenReturn(existingTrack)
        whenever(trackRepository.save(any())).thenReturn(track)

        synchronizeTelegramChannelService.execute(channelId)

        //val telegramPostCaptor = argumentCaptor<TelegramPost>()
        val trackCaptor = argumentCaptor<Track>()

        //verify(telegramPostRepository, times(1)).save(telegramPostCaptor.capture())
        verify(trackRepository, times(1)).save(trackCaptor.capture())

//        val resultPost = telegramPostCaptor.firstValue
//        val resultTrack = resultPost.track
//        val resultTrackFile = resultPost.trackFile
        val resultTrack = trackCaptor.firstValue
        val resultTrackFile = resultTrack.trackFile!!
        val resultPost = resultTrack.telegramPost!!

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
