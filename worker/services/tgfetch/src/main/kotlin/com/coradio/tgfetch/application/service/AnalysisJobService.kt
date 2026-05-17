package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.model.AnalysisJob
import com.coradio.tgfetch.domain.port.out.AnalysisJobRepositoryPort
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AnalysisJobService(
    private val analysisJobRepository: AnalysisJobRepositoryPort
) {
    fun saveJob(job: AnalysisJob): AnalysisJob = analysisJobRepository.save(job)

    fun findJobById(jobId: UUID): AnalysisJob? = analysisJobRepository.findById(jobId)

    fun findAllJobs(): List<AnalysisJob> = analysisJobRepository.findAll()

    fun findAllPendingJobs(): List<AnalysisJob> = analysisJobRepository.findAllByStartedAtIsNull()

    fun deleteById(jobId: UUID): Unit = analysisJobRepository.deleteById(jobId)

}
