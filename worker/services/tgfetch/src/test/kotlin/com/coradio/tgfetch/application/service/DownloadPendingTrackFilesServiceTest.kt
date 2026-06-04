package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.enums.TrackFileStatus
import com.coradio.tgfetch.domain.model.Track
import com.coradio.tgfetch.domain.model.TrackFile
import com.coradio.tgfetch.domain.model.view.TrackFileJobView
import com.coradio.tgfetch.domain.port.out.persistence.TrackFileRepositoryPort
import com.coradio.tgfetch.domain.port.out.storage.AudioMetadataServicePort
import com.coradio.tgfetch.domain.port.out.storage.StorageGatewayPort
import com.coradio.tgfetch.domain.port.out.telegram.TelegramGatewayPort
import com.coradio.tgfetch.infrastructure.exception.StorageException
import com.coradio.tgfetch.infrastructure.exception.TelegramException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class DownloadPendingTrackFilesServiceTest {

    @Mock
    lateinit var trackFileRepository: TrackFileRepositoryPort

    @Mock
    lateinit var telegramGateway: TelegramGatewayPort

    @Mock
    lateinit var storageGateway: StorageGatewayPort

    @Mock
    lateinit var audioMetadataService: AudioMetadataServicePort

    @InjectMocks
    lateinit var downloadPendingTrackFilesService: DownloadPendingTrackFilesService

    lateinit var trackFile: TrackFile

    lateinit var trackFileJobView: TrackFileJobView

    lateinit var tempFile: Path

    @BeforeEach
    fun setUp() {
        val track = Track(
            id = UUID.randomUUID(),
            title = "title",
            artist = "artist",
            duration = 100,
        )

        trackFile = TrackFile(
            id = UUID.randomUUID(),
            etag = "etag",
            telegramFileId = "1234",
            telegramFileUniqueId = "1234",
            fileName = "artist - title.flac",
            fileSize = 100,
            mimeType = "flac",
            status = TrackFileStatus.CREATED,
            retryCount = 0,
            lastDownloadAttemptAt = Instant.now().minus(5, ChronoUnit.MINUTES),
        )

        trackFileJobView = TrackFileJobView(
            id = trackFile.id,
            telegramFileId = trackFile.telegramFileId,
            artist = track.artist,
            title = track.title,
            retryCount = trackFile.retryCount,
        )

        tempFile = Files.createTempFile("track", ".flac")
    }

    @Test
    fun `execute successfully`() = runTest {
        whenever(trackFileRepository.findAllByStatus(TrackFileStatus.PENDING))
            .thenReturn(listOf(trackFileJobView))
        whenever(trackFileRepository.findById(any()))
            .thenReturn(trackFile)

        whenever(telegramGateway.downloadFile(any(), any())).thenReturn(tempFile)

        downloadPendingTrackFilesService.execute()

        val captor = argumentCaptor<TrackFile>()

        verify(trackFileRepository, atLeastOnce()).save(captor.capture())
        assertEquals(TrackFileStatus.READY, captor.allValues.last().status)

        verify(telegramGateway).downloadFile(trackFile.telegramFileId, "flac")
        verify(audioMetadataService).rewriteMetadata(any(), any(), any())

        verify(storageGateway).upload(any(), any())

    }

    @Test
    fun `execute failed on telegram error`() = runTest {
        whenever(trackFileRepository.findAllByStatus(TrackFileStatus.PENDING))
            .thenReturn(listOf(trackFileJobView))
        whenever(trackFileRepository.findById(any()))
            .thenReturn(trackFile)
        whenever(telegramGateway.downloadFile(any(), any()))
            .thenThrow(TelegramException("Telegram unavailable", RuntimeException()))

        downloadPendingTrackFilesService.execute()

        val captor = argumentCaptor<TrackFile>()

        verify(trackFileRepository, atLeastOnce()).save(captor.capture())
        assertEquals(TrackFileStatus.FAILED, captor.allValues.last().status)
    }

    @Test
    fun `execute failed on s3 error`() = runTest {
        whenever(trackFileRepository.findAllByStatus(TrackFileStatus.PENDING))
            .thenReturn(listOf(trackFileJobView))
        whenever(trackFileRepository.findById(any()))
            .thenReturn(trackFile)
        whenever(telegramGateway.downloadFile(any(), any())).thenReturn(tempFile)
        whenever(storageGateway.upload(any(), any()))
            .thenThrow(StorageException("S3 unavailable", RuntimeException()))

        downloadPendingTrackFilesService.execute()

        val captor = argumentCaptor<TrackFile>()

        verify(trackFileRepository, atLeastOnce()).save(captor.capture())
        assertEquals(TrackFileStatus.FAILED, captor.allValues.last().status)
    }

    @Test
    fun `execute on empty list`() = runTest {
        whenever(trackFileRepository.findAllByStatus(TrackFileStatus.PENDING))
            .thenReturn(emptyList())

        downloadPendingTrackFilesService.execute()

        verifyNoInteractions(
            telegramGateway,
            storageGateway,
            audioMetadataService
        )

    }

}
