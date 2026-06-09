package com.coradio.tgfetch.domain.port.`in`

import java.util.UUID

interface ScheduleTrackAnalysisUseCase {
    fun execute(trackFileId: UUID)
}