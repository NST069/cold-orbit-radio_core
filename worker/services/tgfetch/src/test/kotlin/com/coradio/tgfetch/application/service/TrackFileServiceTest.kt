package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.model.Track
import com.coradio.tgfetch.domain.model.TrackFile
import com.coradio.tgfetch.domain.port.out.TrackFileRepositoryPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class TrackFileServiceTest {

    @Mock
    lateinit var trackFileRepository: TrackFileRepositoryPort

    @InjectMocks
    lateinit var trackFileService: TrackFileService

    val trackFile = TrackFile(
        id = UUID.randomUUID(),
        track = Track(title = "title", artist = "artist", duration = 100),
        sha256 = "sha256",
        telegramFileUniqueId = "1234",
        storageKey = "key",
        fileSize = 100,
        mimeType = "flac"
    )

    @Test
    fun `saveTrackFile should save a track file`() {
        whenever(trackFileRepository.save(any())).thenReturn(trackFile)

        val result = trackFileService.saveTrackFile(trackFile)

        assertEquals(trackFile, result)
        assertEquals(trackFile.id, result.id)
        assertEquals(trackFile.track, result.track)
        assertEquals(trackFile.sha256, result.sha256)
        assertEquals(trackFile.telegramFileUniqueId, result.telegramFileUniqueId)
        assertEquals(trackFile.storageKey, result.storageKey)
        assertEquals(trackFile.fileSize, result.fileSize)
        assertEquals(trackFile.mimeType, result.mimeType)

        verify(trackFileRepository, times(1)).save(any())
    }

    @Test
    fun `findAllTrackFiles should return all track files`() {
        val trackFiles = listOf(trackFile, trackFile)
        whenever(trackFileRepository.findAll()).thenReturn(trackFiles)

        val result = trackFileService.findAllTrackFiles()

        assertNotNull(result)
        assertEquals(trackFiles.size, result.size)

        verify(trackFileRepository, times(1)).findAll()
    }

    @Test
    fun `findTrackFileById should return track file`() {
        whenever(trackFileRepository.findById(any())).thenReturn(trackFile)

        val result = trackFileService.findTrackFileById(UUID.randomUUID())

        assertNotNull(result)
        assertEquals(trackFile, result)
        assertEquals(trackFile.id, result.id)
        assertEquals(trackFile.track, result.track)
        assertEquals(trackFile.sha256, result.sha256)
        assertEquals(trackFile.telegramFileUniqueId, result.telegramFileUniqueId)
        assertEquals(trackFile.storageKey, result.storageKey)
        assertEquals(trackFile.fileSize, result.fileSize)
        assertEquals(trackFile.mimeType, result.mimeType)

        verify(trackFileRepository, times(1)).findById(any())
    }

    @Test
    fun `findByTrackId should return track file`() {
        whenever(trackFileRepository.findByTrackId(any())).thenReturn(trackFile)

        val result = trackFileService.findByTrackId(UUID.randomUUID())

        assertNotNull(result)
        assertEquals(trackFile, result)
        assertEquals(trackFile.id, result.id)
        assertEquals(trackFile.track, result.track)
        assertEquals(trackFile.sha256, result.sha256)
        assertEquals(trackFile.telegramFileUniqueId, result.telegramFileUniqueId)
        assertEquals(trackFile.storageKey, result.storageKey)
        assertEquals(trackFile.fileSize, result.fileSize)
        assertEquals(trackFile.mimeType, result.mimeType)

        verify(trackFileRepository, times(1)).findByTrackId(any())
    }

    @Test
    fun `deleteById should remove track file`() {
        doNothing().whenever(trackFileRepository).deleteById(any())

        trackFileService.deleteById(UUID.randomUUID())

        verify(trackFileRepository, times(1)).deleteById(any())
    }

}
