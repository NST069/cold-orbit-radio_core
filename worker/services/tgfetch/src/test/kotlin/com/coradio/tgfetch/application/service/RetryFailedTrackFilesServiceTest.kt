package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.enums.TrackFileStatus
import com.coradio.tgfetch.domain.model.Track
import com.coradio.tgfetch.domain.model.TrackFile
import com.coradio.tgfetch.domain.model.view.TrackFileJobView
import com.coradio.tgfetch.domain.port.out.persistence.TrackFileRepositoryPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class RetryFailedTrackFilesServiceTest {

    @Mock
    lateinit var trackFileRepository: TrackFileRepositoryPort

    @InjectMocks
    lateinit var retryFailedTrackFilesService: RetryFailedTrackFilesService

    lateinit var trackFile: TrackFile

    lateinit var trackFileJobView: TrackFileJobView

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
            status = TrackFileStatus.FAILED,
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
    }

    @Test
    fun `execute from failed to pending`() {
        val trackFilePending = TrackFile(
            id = trackFile.id,
            etag = "etag",
            telegramFileId = "1234",
            telegramFileUniqueId = "1234",
            fileName = "artist - title.flac",
            fileSize = 100,
            mimeType = "flac",
            status = TrackFileStatus.PENDING,
            retryCount = 1,
            lastDownloadAttemptAt = Instant.now(),
        )
        whenever(trackFileRepository.findAllByStatus(TrackFileStatus.FAILED))
            .thenReturn(listOf(trackFileJobView))
        whenever(trackFileRepository.findById(any()))
            .thenReturn(trackFile)
        whenever(trackFileRepository.save(any())).thenReturn(trackFilePending)

        retryFailedTrackFilesService.execute()

        val captor = argumentCaptor<TrackFile>()

        verify(trackFileRepository, atLeastOnce()).save(captor.capture())
        assertEquals(1, captor.allValues.last().retryCount)
        assertEquals(TrackFileStatus.PENDING, captor.allValues.last().status)

    }

    @Test
    fun `execute from failed to failed_permanently`() {
        val trackFileFailedPermanently = TrackFile(
            id = trackFile.id,
            etag = "etag",
            telegramFileId = "1234",
            telegramFileUniqueId = "1234",
            fileName = "artist - title.flac",
            fileSize = 100,
            mimeType = "flac",
            status = TrackFileStatus.FAILED_PERMANENTLY,
            retryCount = 5,
            lastDownloadAttemptAt = Instant.now(),
        )
        whenever(trackFileRepository.findAllByStatus(TrackFileStatus.FAILED))
            .thenReturn(listOf(trackFileJobView.copy(retryCount = 5)))
        whenever(trackFileRepository.findById(any()))
            .thenReturn(trackFile.copy(retryCount = 5))

        whenever(trackFileRepository.save(any())).thenReturn(trackFileFailedPermanently)

        retryFailedTrackFilesService.execute()

        val captor = argumentCaptor<TrackFile>()

        verify(trackFileRepository, atLeastOnce()).save(captor.capture())
        assertEquals(6, captor.allValues.last().retryCount)
        assertEquals(TrackFileStatus.FAILED_PERMANENTLY, captor.allValues.last().status)

    }

    @Test
    fun `execute on empty list`() {
        whenever(trackFileRepository.findAllByStatus(TrackFileStatus.FAILED))
            .thenReturn(emptyList())

        retryFailedTrackFilesService.execute()

        verify(trackFileRepository, never()).save(any())
    }

}
