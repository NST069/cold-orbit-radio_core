package com.coradio.tgfetch.domain.port.out.persistence

import com.coradio.tgfetch.domain.model.AnalysisJob
import java.util.UUID

interface AnalysisJobRepositoryPort {
    fun save(analysisJob: AnalysisJob): AnalysisJob
    fun findById(id: UUID): AnalysisJob?
    fun findAll(): List<AnalysisJob>
    fun findAllByStartedAtIsNull(): List<AnalysisJob>
    fun deleteById(id: UUID)
}
