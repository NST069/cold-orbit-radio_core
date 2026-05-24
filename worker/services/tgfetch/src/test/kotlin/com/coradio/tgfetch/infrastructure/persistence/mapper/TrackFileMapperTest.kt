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
        assertEquals(mockTrackFile.etag, result.etag)
        assertEquals(mockTrackFile.telegramFileId, result.telegramFileId)
        assertEquals(mockTrackFile.telegramFileUniqueId, result.telegramFileUniqueId)
        assertEquals(mockTrackFile.fileSize, result.fileSize)
        assertEquals(mockTrackFile.mimeType, result.mimeType)
        assertEquals(mockTrackFile.storageKey, result.storageKey)
        assertEquals(mockTrackFile.status, result.status)
        assertEquals(mockTrackFile.retryCount, result.retryCount)
        assertEquals(mockTrackFile.lastDownloadAttemptAt, result.lastDownloadAttemptAt)
    }

    @Test
    fun `toEntity should return valid entity`() {
        val result = TrackFileMapper.toEntity(mockTrackFile)

        assertEquals(mockTrackFileEntity.id, result.id)
        assertEquals(mockTrackFileEntity.trackEntity.id, result.trackEntity.id)
        assertEquals(mockTrackFileEntity.etag, result.etag)
        assertEquals(mockTrackFileEntity.telegramFileId, result.telegramFileId)
        assertEquals(mockTrackFileEntity.telegramFileUniqueId, result.telegramFileUniqueId)
        assertEquals(mockTrackFileEntity.fileSize, result.fileSize)
        assertEquals(mockTrackFileEntity.mimeType, result.mimeType)
        assertEquals(mockTrackFileEntity.storageKey, result.storageKey)
        assertEquals(mockTrackFileEntity.status, result.status)
        assertEquals(mockTrackFileEntity.retryCount, result.retryCount)
        assertEquals(mockTrackFileEntity.lastDownloadAttemptAt, result.lastDownloadAttemptAt)
    }

}
