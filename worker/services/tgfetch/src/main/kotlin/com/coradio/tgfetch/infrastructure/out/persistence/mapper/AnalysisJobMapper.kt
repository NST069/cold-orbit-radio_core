package com.coradio.tgfetch.infrastructure.out.persistence.mapper

import com.coradio.tgfetch.domain.model.AnalysisJob
import com.coradio.tgfetch.infrastructure.out.persistence.entity.AnalysisJobEntity

object AnalysisJobMapper {
    fun toDomain(analysisJobEntity: AnalysisJobEntity): AnalysisJob = AnalysisJob(
        id = analysisJobEntity.id,
        trackFile = TrackFileMapper.toDomain(analysisJobEntity.trackFileEntity),
        status = analysisJobEntity.status,
        createdAt = analysisJobEntity.createdAt,
        startedAt = analysisJobEntity.startedAt,
        finishedAt = analysisJobEntity.finishedAt,
        errorMessage = analysisJobEntity.errorMessage,
    )

    fun toEntity(analysisJob: AnalysisJob): AnalysisJobEntity = AnalysisJobEntity().apply {
        this.id = analysisJob.id
        this.trackFileEntity = TrackFileMapper.toEntity(analysisJob.trackFile)
        this.status = analysisJob.status
        this.createdAt = analysisJob.createdAt
        this.startedAt = analysisJob.startedAt
        this.finishedAt = analysisJob.finishedAt
        this.errorMessage = analysisJob.errorMessage
    }
}
