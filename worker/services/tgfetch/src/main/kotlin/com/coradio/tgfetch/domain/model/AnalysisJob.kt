package com.coradio.tgfetch.domain.model

import com.coradio.tgfetch.domain.enums.AnalysisJobStatus
import java.time.Instant
import java.util.UUID

data class AnalysisJob (
    val id: UUID? = null,
    val trackFile: TrackFile,
    var status: AnalysisJobStatus,
    val createdAt: Instant,
    var startedAt: Instant? = null,
    var finishedAt: Instant? = null,
    var errorMessage: String? = null
) {
    fun changeStatus(newStatus: AnalysisJobStatus) {
        this.status = newStatus
        if (newStatus == AnalysisJobStatus.RUNNING)
            this.startedAt = Instant.now()

        if (newStatus == AnalysisJobStatus.COMPLETED || newStatus == AnalysisJobStatus.FAILED)
            this.finishedAt = Instant.now()
    }

    fun changeErrorMessage(newErrorMessage: String) {
        this.errorMessage = newErrorMessage
    }
}
