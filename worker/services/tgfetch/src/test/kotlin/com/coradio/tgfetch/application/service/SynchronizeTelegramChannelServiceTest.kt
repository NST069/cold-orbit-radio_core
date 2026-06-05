package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.application.util.MetadataResolver
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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class SynchronizeTelegramChannelServiceTest {

    @Mock
    lateinit var telegramGateway: TelegramGatewayPort

    @Mock
    lateinit var trackRepository: TrackRepositoryPort

    @Mock
    lateinit var telegramPostRepository: TelegramPostRepositoryPort

    @Mock
    lateinit var metadataResolver: MetadataResolver

    @InjectMocks
    lateinit var service: SynchronizeTelegramChannelService

    private val channelId = 12345L

    private fun telegramTrack() = TelegramTrackData(
        channelId = 1,
        messageId = 10,
        tdFileId = 100,
        remoteFileId = "remote-file-id",
        uniqueFileId = "unique-id",
        artist = "Artist",
        title = "Title",
        rawText = "Artist - Title",
        durationSeconds = 180,
        fileSizeBytes = 12345,
        fileName = "track.mp3",
        mimeType = "audio/mpeg",
        coverTdFileId = 0,
        coverRemoteFileId = null,
        coverUniqueFileId = null,
        publishedAt = Instant.now()
    )

    @Test
    fun `should create new track when telegram post does not exist`() = runTest {

        val telegramTrack = telegramTrack()

        whenever(
            telegramGateway.getMessages(any(), any(), any())
        ).thenReturn(
            MessagePageData(
                items = listOf(telegramTrack),
                nextCursor = null,
                hasMore = false
            )
        )

        whenever(
            telegramPostRepository.findByChannelAndMessageId(any(), any())
        ).thenReturn(null)

        whenever(
            metadataResolver.resolve(any())
        ).thenReturn(
            TrackMetadata(
                artist = "Artist",
                title = "Title"
            )
        )

        service.execute(channelId, 100)

        verify(trackRepository).save(any())

        verify(metadataResolver).resolve(telegramTrack)
    }

    @Test
    fun `should use default limit if not provided`() = runTest {

        val telegramTrack = telegramTrack()

        whenever(
            telegramGateway.getMessages(any(), any(), any())
        ).thenReturn(
            MessagePageData(
                items = listOf(telegramTrack),
                nextCursor = null,
                hasMore = false
            )
        )

        whenever(
            telegramPostRepository.findByChannelAndMessageId(any(), any())
        ).thenReturn(null)

        whenever(
            metadataResolver.resolve(any())
        ).thenReturn(
            TrackMetadata(
                artist = "Artist",
                title = "Title"
            )
        )

        service.execute(channelId)

        verify(trackRepository).save(any())

        verify(metadataResolver).resolve(telegramTrack)
    }

    @Test
    fun `should update existing track when incoming data differs`() = runTest {

        val trackId = UUID.randomUUID()

        val incoming = telegramTrack()

        val existingTrack = Track(
            id = trackId,
            artist = "Old Artist",
            title = "Old Title",
            duration = 1
        )

        whenever(
            telegramGateway.getMessages(any(), any(), any())
        ).thenReturn(
            MessagePageData(
                items = listOf(incoming),
                nextCursor = null,
                hasMore = false
            )
        )

        whenever(
            telegramPostRepository.findByChannelAndMessageId(any(), any())
        ).thenReturn(
            TelegramPostView(
                id = UUID.randomUUID(),
                trackId = trackId
            )
        )

        whenever(trackRepository.findById(trackId))
            .thenReturn(existingTrack)

        whenever(trackRepository.save(any()))
            .thenReturn(existingTrack)

        service.execute(channelId, 100)

        verify(trackRepository).save(existingTrack)
    }

    @Test
    fun `should skip track when no changes detected`() = runTest {

        val trackId = UUID.randomUUID()

        val incoming = telegramTrack()

        val existingTrack = Track(
            id = trackId,
            artist = incoming.artist!!,
            title = incoming.title!!,
            duration = incoming.durationSeconds!!
        )

        existingTrack.attachTrackFile(
            TrackFile(
                etag = "etag",
                telegramFileId = incoming.remoteFileId,
                telegramFileUniqueId = incoming.uniqueFileId,
                fileName = incoming.fileName!!,
                fileSize = incoming.fileSizeBytes!!,
                mimeType = incoming.mimeType!!
            )
        )

        whenever(
            telegramGateway.getMessages(any(), any(), any())
        ).thenReturn(
            MessagePageData(
                items = listOf(incoming),
                nextCursor = null,
                hasMore = false
            )
        )

        whenever(
            telegramPostRepository.findByChannelAndMessageId(any(), any())
        ).thenReturn(
            TelegramPostView(
                id = UUID.randomUUID(),
                trackId = trackId
            )
        )

        whenever(trackRepository.findById(trackId))
            .thenReturn(existingTrack)

        service.execute(channelId, 100)

        verify(trackRepository, never()).save(any())
    }

    @Test
    fun `should ignore items without remote file id`() = runTest {

        val track = telegramTrack().copy(
            remoteFileId = ""
        )

        whenever(
            telegramGateway.getMessages(any(), any(), any())
        ).thenReturn(
            MessagePageData(
                items = listOf(track),
                nextCursor = null,
                hasMore = false
            )
        )

        service.execute(channelId, 100)

        verifyNoInteractions(trackRepository)
        verifyNoInteractions(telegramPostRepository)
    }

    @Test
    fun `should process all pages`() = runTest {

        whenever(
            telegramGateway.getMessages(channelId, 100, 0)
        ).thenReturn(
            MessagePageData(
                items = listOf(telegramTrack()),
                nextCursor = 100,
                hasMore = true
            )
        )

        whenever(
            telegramGateway.getMessages(channelId, 100, 100)
        ).thenReturn(
            MessagePageData(
                items = listOf(telegramTrack()),
                nextCursor = null,
                hasMore = false
            )
        )

        whenever(
            telegramPostRepository.findByChannelAndMessageId(any(), any())
        ).thenReturn(null)

        whenever(
            metadataResolver.resolve(any())
        ).thenReturn(
            TrackMetadata(
                artist = "Artist",
                title = "Title"
            )
        )

        service.execute(channelId, 100)

        verify(telegramGateway, times(2))
            .getMessages(any(), any(), any())

        verify(trackRepository, times(2))
            .save(any())
    }

}
