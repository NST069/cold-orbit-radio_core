package com.coradio.tgfetch.infrastructure.persistence.mapper

import com.coradio.tgfetch.infrastructure.persistence.mapper.MockEntities.mockAnalysisJob
import com.coradio.tgfetch.infrastructure.persistence.mapper.MockEntities.mockAnalysisJobEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AnalysisJobMapperTest {

    @Test
    fun `toDomain should return valid domain object`() {
        val result = AnalysisJobMapper.toDomain(mockAnalysisJobEntity)

        assertEquals(mockAnalysisJob.id, result.id)
        assertEquals(mockAnalysisJob.trackFile.id, result.trackFile.id)
        assertEquals(mockAnalysisJob.createdAt, result.createdAt)
        assertEquals(mockAnalysisJob.startedAt, result.startedAt)
        assertEquals(mockAnalysisJob.finishedAt, result.finishedAt)
        assertEquals(mockAnalysisJob.errorMessage, result.errorMessage)
    }

    @Test
    fun `toEntity should return valid entity`() {
        val result = AnalysisJobMapper.toEntity(mockAnalysisJob)

        assertEquals(mockAnalysisJobEntity.id, result.id)
        assertEquals(mockAnalysisJobEntity.trackFileEntity.id, result.trackFileEntity.id)
        assertEquals(mockAnalysisJobEntity.createdAt, result.createdAt)
        assertEquals(mockAnalysisJobEntity.startedAt, result.startedAt)
        assertEquals(mockAnalysisJobEntity.finishedAt, result.finishedAt)
        assertEquals(mockAnalysisJobEntity.errorMessage, result.errorMessage)
    }

}
