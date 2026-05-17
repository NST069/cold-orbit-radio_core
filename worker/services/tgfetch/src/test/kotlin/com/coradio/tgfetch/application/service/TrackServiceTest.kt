package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.model.Track
import com.coradio.tgfetch.domain.port.out.TrackRepositoryPort
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class TrackServiceTest {

    @Mock
    lateinit var trackRepository: TrackRepositoryPort

    @InjectMocks
    lateinit var trackService: TrackService

    val track = Track(
        id = UUID.randomUUID(),
        title = "Track Title",
        artist = "Artist",
        duration = 100
    )

    @Test
    fun `addTrack should save track entity`() {
        whenever(trackRepository.save(any())).thenReturn(track)

        val result = trackService.addTrack(track)

        assertEquals(track, result)
        assertEquals(track.id, result.id)
        assertEquals(track.title, result.title)
        assertEquals(track.artist, result.artist)
        assertEquals(track.duration, result.duration)

        verify(trackRepository, times(1)).save(any())
    }

    @Test
    fun `getTrackById should return track`() {
        whenever(trackRepository.findById(any())).thenReturn(track)

        val result = trackService.getTrackById(UUID.randomUUID())

        assertNotNull(result)
        assertEquals(track, result)
        assertEquals(track.id, result.id)
        assertEquals(track.title, result.title)
        assertEquals(track.artist, result.artist)
        assertEquals(track.duration, result.duration)

        verify(trackRepository, times(1)).findById(any())
    }

    @Test
    fun `getTrackByTitleAndArtist should return track`() {
        whenever(trackRepository.findByTitleAndArtist(anyString(), anyString())).thenReturn(track)

        val result = trackService.getTrackByTitleAndArtist(track.title, track.artist)

        assertNotNull(result)
        assertEquals(track, result)
        assertEquals(track.id, result.id)
        assertEquals(track.title, result.title)
        assertEquals(track.artist, result.artist)
        assertEquals(track.duration, result.duration)

        verify(trackRepository, times(1)).findByTitleAndArtist(anyString(), anyString())
    }

    @Test
    fun `getAllTracks should return all tracks`() {
        val tracks = listOf(track, track)
        whenever(trackRepository.findAll()).thenReturn(tracks)

        val result = trackService.getAllTracks()

        assertNotNull(result)
        assertEquals(tracks.size, result.size)

        verify(trackRepository, times(1)).findAll()
    }

    @Test
    fun `deleteById should remove by id`() {
        doNothing().whenever(trackRepository).deleteById(any())

        trackService.deleteById(UUID.randomUUID())

        verify(trackRepository, times(1)).deleteById(any())
    }

}
