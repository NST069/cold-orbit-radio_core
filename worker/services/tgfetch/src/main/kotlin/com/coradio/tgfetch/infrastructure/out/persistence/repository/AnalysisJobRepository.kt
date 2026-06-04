package com.coradio.tgfetch.infrastructure.out.persistence.repository

import com.coradio.tgfetch.domain.enums.AnalysisJobStatus
import com.coradio.tgfetch.infrastructure.out.persistence.entity.AnalysisJobEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AnalysisJobRepository : JpaRepository<AnalysisJobEntity, UUID> {
    fun findAllByStartedAtIsNull(): List<AnalysisJobEntity>

    fun findAllByStatus(
        status: AnalysisJobStatus
    ): List<AnalysisJobEntity>
}
