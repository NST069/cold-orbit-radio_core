package com.coradio.tgfetch.infrastructure.out.persistence.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TrackFileMapperTest {

    @Test
    fun `toDomain should return valid domain object`() {
        val result = TrackFileMapper.toDomain(MockEntities.mockTrackFileEntity)

        assertEquals(MockEntities.mockTrackFile.id, result.id)
        assertEquals(MockEntities.mockTrackFile.etag, result.etag)
        assertEquals(MockEntities.mockTrackFile.telegramFileId, result.telegramFileId)
        assertEquals(MockEntities.mockTrackFile.telegramFileUniqueId, result.telegramFileUniqueId)
        assertEquals(MockEntities.mockTrackFile.fileName, result.fileName)
        assertEquals(MockEntities.mockTrackFile.fileSize, result.fileSize)
        assertEquals(MockEntities.mockTrackFile.mimeType, result.mimeType)
        assertEquals(MockEntities.mockTrackFile.storageKey, result.storageKey)
        assertEquals(MockEntities.mockTrackFile.status, result.status)
        assertEquals(MockEntities.mockTrackFile.retryCount, result.retryCount)
        assertEquals(MockEntities.mockTrackFile.lastDownloadAttemptAt, result.lastDownloadAttemptAt)
    }

    @Test
    fun `toEntity should return valid entity`() {
        val result = TrackFileMapper.toEntity(MockEntities.mockTrackFile)

        assertEquals(MockEntities.mockTrackFileEntity.id, result.id)
        assertEquals(MockEntities.mockTrackFileEntity.etag, result.etag)
        assertEquals(MockEntities.mockTrackFileEntity.telegramFileId, result.telegramFileId)
        assertEquals(MockEntities.mockTrackFileEntity.telegramFileUniqueId, result.telegramFileUniqueId)
        assertEquals(MockEntities.mockTrackFile.fileName, result.fileName)
        assertEquals(MockEntities.mockTrackFileEntity.fileSize, result.fileSize)
        assertEquals(MockEntities.mockTrackFileEntity.mimeType, result.mimeType)
        assertEquals(MockEntities.mockTrackFileEntity.storageKey, result.storageKey)
        assertEquals(MockEntities.mockTrackFileEntity.status, result.status)
        assertEquals(MockEntities.mockTrackFileEntity.retryCount, result.retryCount)
        assertEquals(MockEntities.mockTrackFileEntity.lastDownloadAttemptAt, result.lastDownloadAttemptAt)
    }

}
