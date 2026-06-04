package com.coradio.tgfetch.domain.port.`in`

import com.coradio.tgfetch.domain.model.valueobject.SynchronizationSummary

interface SynchronizeTelegramChannelUseCase {
    suspend fun execute(channelId: Long, limit: Int = 100): SynchronizationSummary
}
