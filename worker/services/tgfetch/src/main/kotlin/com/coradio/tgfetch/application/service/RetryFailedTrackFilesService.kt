package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.enums.TrackFileStatus
import com.coradio.tgfetch.domain.port.`in`.RetryFailedTrackFilesUseCase
import com.coradio.tgfetch.domain.port.out.persistence.TrackFileRepositoryPort

class RetryFailedTrackFilesService(
    private val trackFileRepository: TrackFileRepositoryPort,
): RetryFailedTrackFilesUseCase {
    override fun execute() {
        val failedTracks = trackFileRepository.findAllByStatus(TrackFileStatus.FAILED)

        failedTracks.forEach { trackFile ->
            trackFile.retry()
            trackFile.changeStatus(
                if (trackFile.retryCount <= 5)
                    TrackFileStatus.PENDING
                else TrackFileStatus.FAILED_PERMANENTLY
            )

            trackFileRepository.save(trackFile)
        }
    }
}
