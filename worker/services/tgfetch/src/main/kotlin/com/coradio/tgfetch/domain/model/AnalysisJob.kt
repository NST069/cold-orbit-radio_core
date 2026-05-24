package com.coradio.tgfetch.domain.model

import com.coradio.tgfetch.domain.enums.AnalysisJobStatus
import java.time.Instant
import java.util.UUID

data class AnalysisJob (
    val id: UUID? = null,
    val trackFile: TrackFile,
    val status: AnalysisJobStatus,
    val createdAt: Instant,
    val startedAt: Instant? = null,
    val finishedAt: Instant? = null,
    val errorMessage: String? = null
)
