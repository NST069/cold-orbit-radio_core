package com.coradio.tgfetch.infrastructure.out.persistence.adapter

import com.coradio.tgfetch.domain.enums.AnalysisJobStatus
import com.coradio.tgfetch.domain.model.AnalysisJob
import com.coradio.tgfetch.domain.port.out.persistence.AnalysisJobRepositoryPort
import com.coradio.tgfetch.infrastructure.out.persistence.mapper.AnalysisJobMapper
import com.coradio.tgfetch.infrastructure.out.persistence.repository.AnalysisJobRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AnalysisJobAdapter(
    private val analysisJobRepository: AnalysisJobRepository,
): AnalysisJobRepositoryPort {
    override fun save(analysisJob: AnalysisJob): AnalysisJob {
        val entity = AnalysisJobMapper.toEntity(analysisJob)
        val saved = analysisJobRepository.save(entity)
        return AnalysisJobMapper.toDomain(saved)
    }

    override fun findById(id: UUID): AnalysisJob? {
        return analysisJobRepository.findById(id)
            .map(AnalysisJobMapper::toDomain)
            .orElse(null)
    }

    override fun findAll(): List<AnalysisJob> {
        return analysisJobRepository.findAll()
            .map(AnalysisJobMapper::toDomain)
    }

    override fun findAllByStartedAtIsNull(): List<AnalysisJob> {
        return analysisJobRepository.findAllByStartedAtIsNull()
            .map(AnalysisJobMapper::toDomain)
    }

    override fun deleteById(id: UUID) {
        analysisJobRepository.deleteById(id)
    }

    override fun findAllByStatus(status: AnalysisJobStatus): List<AnalysisJob> {
        return analysisJobRepository.findAllByStatus(status)
            .map(AnalysisJobMapper::toDomain)
    }
}
