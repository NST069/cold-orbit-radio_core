package com.coradio.tgfetch.infrastructure.persistence.mapper

import com.coradio.tgfetch.infrastructure.persistence.mapper.MockEntities.mockTrackFile
import com.coradio.tgfetch.infrastructure.persistence.mapper.MockEntities.mockTrackFileEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TrackFileMapperTest {

    @Test
    fun `toDomain should return valid domain object`() {
        val result = TrackFileMapper.toDomain(mockTrackFileEntity)

        assertEquals(mockTrackFile.id, result.id)
        assertEquals(mockTrackFile.track.id, result.track.id)
        assertEquals(mockTrackFile.fileSize, result.fileSize)
        assertEquals(mockTrackFile.telegramFileUniqueId, result.telegramFileUniqueId)
        assertEquals(mockTrackFile.sha256, result.sha256)
        assertEquals(mockTrackFile.mimeType, result.mimeType)
        assertEquals(mockTrackFile.storageKey, result.storageKey)
    }

    @Test
    fun `toEntity should return valid entity`() {
        val result = TrackFileMapper.toEntity(mockTrackFile)

        assertEquals(mockTrackFileEntity.id, result.id)
        assertEquals(mockTrackFileEntity.trackEntity.id, result.trackEntity.id)
        assertEquals(mockTrackFileEntity.fileSize, result.fileSize)
        assertEquals(mockTrackFileEntity.telegramFileUniqueId, result.telegramFileUniqueId)
        assertEquals(mockTrackFileEntity.sha256, result.sha256)
        assertEquals(mockTrackFileEntity.mimeType, result.mimeType)
        assertEquals(mockTrackFileEntity.storageKey, result.storageKey)
    }

}