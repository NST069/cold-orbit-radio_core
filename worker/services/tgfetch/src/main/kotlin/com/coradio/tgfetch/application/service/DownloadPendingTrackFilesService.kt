package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.port.`in`.DownloadPendingTrackFilesUseCase

class DownloadPendingTrackFilesService: DownloadPendingTrackFilesUseCase {
    override fun execute(limit: Int) {
        TODO("Not yet implemented")
        /**
         * Найти TrackFile(PENDING)
         * ↓
         * Скачать из Telegram
         * ↓
         * Обновить теги
         * ↓
         * Загрузить в S3
         * ↓
         * READY
         *
         * или
         *
         * FAILED
         */
    }
}