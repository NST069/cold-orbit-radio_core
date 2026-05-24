package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.port.`in`.SynchronizeTelegramChannelUseCase

class SynchronizeTelegramChannelService: SynchronizeTelegramChannelUseCase {
    override fun execute() {
        TODO("Not yet implemented")
        /**
         * Получить последние сообщения из Telegram
         * ↓
         * Для каждого сообщения
         * ↓
         * Проверить наличие TelegramPost
         * ↓
         * Если нет:
         *     создать Track
         *     создать TrackFile(PENDING)
         *     создать TelegramPost
         * ↓
         * Если есть:
         *     проверить изменения
         *     при необходимости создать новый TrackFile
         */
    }
}