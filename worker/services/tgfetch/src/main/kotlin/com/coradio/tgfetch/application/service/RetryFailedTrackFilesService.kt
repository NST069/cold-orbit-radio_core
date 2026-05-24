package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.port.`in`.RetryFailedTrackFilesUseCase

class RetryFailedTrackFilesService: RetryFailedTrackFilesUseCase {
    override fun execute() {
        TODO("Not yet implemented")
        /**
         * Найти FAILED
         * ↓
         * retry_count < MAX
         * ↓
         * повторить загрузку
         */
    }
}