package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.enums.TrackFileStatus
import com.coradio.tgfetch.domain.model.view.TrackFileJobView
import com.coradio.tgfetch.domain.port.out.persistence.TrackFileRepositoryPort
import com.coradio.tgfetch.domain.port.out.storage.AudioMetadataServicePort
import com.coradio.tgfetch.domain.port.out.storage.StorageGatewayPort
import com.coradio.tgfetch.domain.port.out.telegram.TelegramGatewayPort
import com.coradio.tgfetch.infrastructure.exception.AudioMetadataException
import com.coradio.tgfetch.infrastructure.exception.StorageException
import com.coradio.tgfetch.infrastructure.exception.TelegramException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.startsWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.nio.file.Files
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
    lateinit var service: DownloadPendingTrackFilesService

    private fun mockTrack(id: UUID) =
        TrackFileJobView(
            id = id,
            telegramFileId = "telegram-file-id",
            artist = "Artist",
            title = "Title",
            mimeType = "audio/mpeg",
            fileName = "track.mp3",
            retryCount = 0,
        )

    @Test
    fun `should return empty summary when no pending files`() {
        whenever(
            trackFileRepository.findAllByStatus(TrackFileStatus.PENDING)
        ).thenReturn(emptyList())

        val result = service.execute()

        assertEquals(0, result.success)
        assertEquals(0, result.failed)

        verifyNoInteractions(
            telegramGateway,
            storageGateway,
            audioMetadataService
        )
    }

    @Test
    fun `should skip file when another worker picked it`() {
        val id = UUID.randomUUID()

        val track = mockTrack(id)

        whenever(trackFileRepository.findAllByStatus(TrackFileStatus.PENDING))
            .thenReturn(listOf(track))

        whenever(
            trackFileRepository.updateStatus(
                id,
                TrackFileStatus.DOWNLOADING,
                TrackFileStatus.PENDING
            )
        ).thenReturn(0)

        val result = service.execute()

        assertEquals(0, result.success)
        assertEquals(0, result.failed)

        verifyNoInteractions(
            telegramGateway,
            storageGateway,
            audioMetadataService
        )
    }

    @Test
    fun `should download and upload track`() {
        val id = UUID.randomUUID()

        val track = mockTrack(id)

        val tempFile = Files.createTempFile("test", ".mp3")

        whenever(trackFileRepository.findAllByStatus(TrackFileStatus.PENDING))
            .thenReturn(listOf(track))

        whenever(
            trackFileRepository.updateStatus(
                id,
                TrackFileStatus.DOWNLOADING,
                TrackFileStatus.PENDING
            )
        ).thenReturn(1)

        whenever(
            telegramGateway.downloadFile("telegram-file-id", "mp3")
        ).thenReturn(tempFile)

        whenever(storageGateway.exists(any()))
            .thenReturn(false)

        val result = service.execute()

        assertEquals(1, result.success)
        assertEquals(0, result.failed)

        verify(audioMetadataService)
            .rewriteMetadata(tempFile, "Artist", "Title")

        verify(storageGateway)
            .upload(startsWith("tracks/$id"), eq(tempFile))

        verify(trackFileRepository)
            .markReady(eq(id), any())
    }

    @Test
    fun `should delete existing file before upload`() {
        val id = UUID.randomUUID()

        val track = mockTrack(id)

        val tempFile = Files.createTempFile("test", ".mp3")

        whenever(trackFileRepository.findAllByStatus(TrackFileStatus.PENDING))
            .thenReturn(listOf(track))

        whenever(
            trackFileRepository.updateStatus(
                id,
                TrackFileStatus.DOWNLOADING,
                TrackFileStatus.PENDING
            )
        ).thenReturn(1)

        whenever(
            telegramGateway.downloadFile(any(), any())
        ).thenReturn(tempFile)

        whenever(storageGateway.exists(any()))
            .thenReturn(true)

        service.execute()

        verify(storageGateway).delete(any())
        verify(storageGateway).upload(any(), eq(tempFile))
    }

    @Test
    fun `should mark file as failed when telegram exception occurs`() {
        val id = UUID.randomUUID()

        val track = mockTrack(id)

        whenever(trackFileRepository.findAllByStatus(TrackFileStatus.PENDING))
            .thenReturn(listOf(track))

        whenever(
            trackFileRepository.updateStatus(
                id,
                TrackFileStatus.DOWNLOADING,
                TrackFileStatus.PENDING
            )
        ).thenReturn(1)

        whenever(
            telegramGateway.downloadFile(any(), any())
        ).thenThrow(TelegramException("download failed"))

        val result = service.execute()

        assertEquals(0, result.success)
        assertEquals(1, result.failed)

        verify(trackFileRepository)
            .updateStatus(
                id,
                TrackFileStatus.FAILED,
                TrackFileStatus.DOWNLOADING
            )

        verify(trackFileRepository, never())
            .markReady(any(), any())
    }

    @Test
    fun `should mark file as failed when s3 exception occurs`() {
        val id = UUID.randomUUID()

        val track = mockTrack(id)

        val tempFile = Files.createTempFile("test", ".mp3")

        whenever(trackFileRepository.findAllByStatus(TrackFileStatus.PENDING))
            .thenReturn(listOf(track))

        whenever(
            trackFileRepository.updateStatus(
                id,
                TrackFileStatus.DOWNLOADING,
                TrackFileStatus.PENDING
            )
        ).thenReturn(1)

        whenever(
            telegramGateway.downloadFile("telegram-file-id", "mp3")
        ).thenReturn(tempFile)

        whenever(
            storageGateway.exists(any())
        ).thenThrow(StorageException("storage unavailable", RuntimeException()))

        val result = service.execute()

        assertEquals(0, result.success)
        assertEquals(1, result.failed)

        verify(trackFileRepository)
            .updateStatus(
                id,
                TrackFileStatus.FAILED,
                TrackFileStatus.DOWNLOADING
            )

        verify(trackFileRepository, never())
            .markReady(any(), any())
    }

    @Test
    fun `should mark file as failed when metadata exception occurs`() {
        val id = UUID.randomUUID()

        val track = mockTrack(id)

        val tempFile = Files.createTempFile("test", ".mp3")

        whenever(trackFileRepository.findAllByStatus(TrackFileStatus.PENDING))
            .thenReturn(listOf(track))

        whenever(
            trackFileRepository.updateStatus(
                id,
                TrackFileStatus.DOWNLOADING,
                TrackFileStatus.PENDING
            )
        ).thenReturn(1)

        whenever(
            telegramGateway.downloadFile("telegram-file-id", "mp3")
        ).thenReturn(tempFile)

        whenever(
            audioMetadataService.rewriteMetadata(any(), any(), any())
        ).thenThrow(AudioMetadataException("changing metadata failed", RuntimeException()))

        val result = service.execute()

        assertEquals(0, result.success)
        assertEquals(1, result.failed)

        verify(trackFileRepository)
            .updateStatus(
                id,
                TrackFileStatus.FAILED,
                TrackFileStatus.DOWNLOADING
            )

        verify(trackFileRepository, never())
            .markReady(any(), any())
    }
}
