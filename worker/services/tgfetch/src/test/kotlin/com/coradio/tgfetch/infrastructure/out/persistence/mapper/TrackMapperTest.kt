package com.coradio.tgfetch.infrastructure.out.persistence.mapper

import com.coradio.tgfetch.domain.model.Track
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TrackMapperTest {

    @Test
    fun `toDomain should return valid domain object`() {
        val result = TrackMapper.toDomain(MockEntities.mockTrackEntity)

        assertEquals(MockEntities.mockTrack.id, result.id)
        assertEquals(MockEntities.mockTrack.title, result.title)
        assertEquals(MockEntities.mockTrack.artist, result.artist)
        assertEquals(MockEntities.mockTrack.duration, result.duration)
    }

    @Test
    fun `toEntity should return valid entity`() {
        val result = TrackMapper.toEntity(MockEntities.mockTrack)

        assertEquals(MockEntities.mockTrackEntity.id, result.id)
        assertEquals(MockEntities.mockTrackEntity.title, result.title)
        assertEquals(MockEntities.mockTrackEntity.artist, result.artist)
        assertEquals(MockEntities.mockTrackEntity.duration, result.duration)
    }

    @Test
    fun `toEntity should fill empty title and empty artist`() {
        val track = Track(
            id = MockEntities.mockTrack.id,
            title = "",
            artist = "",
            duration = MockEntities.mockTrack.duration,
        )
        val result = TrackMapper.toEntity(track)

        assertEquals(MockEntities.mockTrackEntity.id, result.id)
        assertEquals("<Без названия>", result.title)
        assertEquals("<Неизвестен>", result.artist)
        assertEquals(MockEntities.mockTrackEntity.duration, result.duration)
    }

}
