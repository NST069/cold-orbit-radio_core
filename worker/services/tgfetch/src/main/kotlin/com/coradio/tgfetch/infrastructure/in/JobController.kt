package com.coradio.tgfetch.infrastructure.`in`

import com.coradio.tgfetch.domain.model.valueobject.DownloadSummary
import com.coradio.tgfetch.domain.model.valueobject.SynchronizationSummary
import com.coradio.tgfetch.domain.port.`in`.DownloadPendingTrackFilesUseCase
import com.coradio.tgfetch.domain.port.`in`.RetryFailedTrackFilesUseCase
import com.coradio.tgfetch.domain.port.`in`.SynchronizeTelegramChannelUseCase
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin")
class JobController(
    private val synchronizeTelegramChannelUseCase: SynchronizeTelegramChannelUseCase,
    private val downloadPendingTrackFilesUseCase: DownloadPendingTrackFilesUseCase,
    private val retryFailedTrackFilesUseCase: RetryFailedTrackFilesUseCase,

    @Value("\${telegram.channel-id}")
    private val channelId: Long
) {

    @PostMapping("/sync-telegram")
    suspend fun syncMessages(): SynchronizationSummary{
        return synchronizeTelegramChannelUseCase.execute(channelId)
    }

    @PostMapping("/download-pending")
    suspend fun downloadPendingTracks(): DownloadSummary {
        return downloadPendingTrackFilesUseCase.execute()
    }

    @PostMapping("/retry-failed")
    suspend fun retryFailedTracks(){
        return retryFailedTrackFilesUseCase.execute()
    }
}