package com.coradio.tgfetch.infrastructure.persistence.mapper

import com.coradio.tgfetch.domain.model.Track
import com.coradio.tgfetch.infrastructure.persistence.mapper.MockEntities.mockTrack
import com.coradio.tgfetch.infrastructure.persistence.mapper.MockEntities.mockTrackEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TrackMapperTest {

    @Test
    fun `toDomain should return valid domain object`() {
        val result = TrackMapper.toDomain(mockTrackEntity)

        assertEquals(mockTrack.id, result.id)
        assertEquals(mockTrack.title, result.title)
        assertEquals(mockTrack.artist, result.artist)
        assertEquals(mockTrack.duration, result.duration)
    }

    @Test
    fun `toEntity should return valid entity`() {
        val result = TrackMapper.toEntity(mockTrack)

        assertEquals(mockTrackEntity.id, result.id)
        assertEquals(mockTrackEntity.title, result.title)
        assertEquals(mockTrackEntity.artist, result.artist)
        assertEquals(mockTrackEntity.duration, result.duration)
    }

    @Test
    fun `toEntity should fill empty title and empty artist`() {
        val track = Track(
            id = mockTrack.id,
            title = "",
            artist = "",
            duration = mockTrack.duration
        )
        val result = TrackMapper.toEntity(track)

        assertEquals(mockTrackEntity.id, result.id)
        assertEquals("<Без названия>", result.title)
        assertEquals("<Неизвестен>", result.artist)
        assertEquals(mockTrackEntity.duration, result.duration)
    }

}
