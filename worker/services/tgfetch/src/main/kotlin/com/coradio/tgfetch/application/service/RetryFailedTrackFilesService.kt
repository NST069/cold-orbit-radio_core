package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.enums.TrackFileStatus
import com.coradio.tgfetch.domain.port.`in`.RetryFailedTrackFilesUseCase
import com.coradio.tgfetch.domain.port.out.persistence.TrackFileRepositoryPort
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RetryFailedTrackFilesService(
    private val trackFileRepository: TrackFileRepositoryPort,
): RetryFailedTrackFilesUseCase {

    private val log = logger {}

    override fun execute() {
        val failedTracks = trackFileRepository.findAllByStatus(TrackFileStatus.FAILED)

        failedTracks.forEach { trackFileView ->
            trackFileView.id
                ?.let {
                    if (trackFileView.retryCount <= 5) {
                        incrementRetry(trackFileView.id)
                        markPendingFromFailed(trackFileView.id)
                    } else {
                        log.error { "Failed Permanently: ${trackFileView.artist} - ${trackFileView.title}" }
                        markFailedPermanently(trackFileView.id)
                    }
                }
        }

        trackFileRepository.findAllByStatus(TrackFileStatus.CREATED)
            .forEach { trackFileView ->
                trackFileView.id
                    ?.let {
                        markPendingFromCreated(trackFileView.id)
                    }
            }
    }

    fun incrementRetry(id: UUID) {
        trackFileRepository.incrementRetry(id)
    }

    fun markPendingFromFailed(id: UUID) {
        trackFileRepository.updateStatus(id, TrackFileStatus.PENDING, TrackFileStatus.FAILED)
    }

    fun markPendingFromCreated(id: UUID) {
        trackFileRepository.updateStatus(id, TrackFileStatus.PENDING, TrackFileStatus.CREATED)
    }

    fun markFailedPermanently(id: UUID) {
        trackFileRepository.updateStatus(id, TrackFileStatus.FAILED_PERMANENTLY, TrackFileStatus.FAILED)
    }

}
