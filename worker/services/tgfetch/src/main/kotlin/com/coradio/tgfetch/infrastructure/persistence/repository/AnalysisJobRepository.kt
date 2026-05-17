package com.coradio.tgfetch.infrastructure.persistence.repository

import com.coradio.tgfetch.infrastructure.persistence.entity.AnalysisJobEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AnalysisJobRepository : JpaRepository<AnalysisJobEntity, UUID> {
    fun findAllByStartedAtIsNull(): List<AnalysisJobEntity>
}
