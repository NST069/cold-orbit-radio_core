package com.coradio.tgfetch.domain.model

import java.time.OffsetDateTime
import java.util.UUID

data class AnalysisJob (
    val id: UUID? = null,
    val trackFile: TrackFile,
    val createdAt: OffsetDateTime,
    val startedAt: OffsetDateTime? = null,
    val finishedAt: OffsetDateTime? = null,
    val errorMessage: String? = null
)
