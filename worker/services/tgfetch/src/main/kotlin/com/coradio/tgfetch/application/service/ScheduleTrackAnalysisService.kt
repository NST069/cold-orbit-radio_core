package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.port.`in`.ScheduleTrackAnalysisUseCase
import java.util.UUID

class ScheduleTrackAnalysisService: ScheduleTrackAnalysisUseCase {
    override fun execute(trackFileId: UUID) {
        TODO("Not yet implemented")
        /**
         * TrackFile READY
         * ↓
         * создать AnalysisJob(PENDING)
         */
    }
}