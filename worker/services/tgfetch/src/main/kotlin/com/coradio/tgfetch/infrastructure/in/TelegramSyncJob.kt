package com.coradio.tgfetch.infrastructure.`in`

import com.coradio.tgfetch.domain.port.`in`.DownloadPendingTrackFilesUseCase
import com.coradio.tgfetch.domain.port.`in`.RetryFailedTrackFilesUseCase
import com.coradio.tgfetch.domain.port.`in`.SynchronizeTelegramChannelUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TelegramSyncJob(
    private val synchronizeTelegramChannelUseCase: SynchronizeTelegramChannelUseCase,
    private val downloadPendingTrackFilesUseCase: DownloadPendingTrackFilesUseCase,
    private val retryFailedTrackFilesUseCase: RetryFailedTrackFilesUseCase,

    @Value("\${telegram.channel-id}")
    private val channelId: Long
) {
    private val log = KotlinLogging.logger {}

    @Scheduled(cron = "\${jobs.sync.cron}")
    suspend fun executeSynchronizeTelegramChannel() {

        log.info{"Telegram sync started"}

        synchronizeTelegramChannelUseCase.execute(channelId)

        log.info{"Telegram sync finished"}
    }

    @Scheduled(cron = "\${jobs.downloads.cron}")
    suspend fun executeDownloadPendingFiles() {

        log.info{"Downloading pending files"}

        downloadPendingTrackFilesUseCase.execute()

        log.info{"Downloading pending files finished"}
    }

    @Scheduled(cron = "\${jobs.retry.cron}")
    suspend fun executeRetryFailedFiles() {

        log.info{"Marking failed to retry"}

        retryFailedTrackFilesUseCase.execute()

        log.info{"Marking failed to retry finished"}
    }

}