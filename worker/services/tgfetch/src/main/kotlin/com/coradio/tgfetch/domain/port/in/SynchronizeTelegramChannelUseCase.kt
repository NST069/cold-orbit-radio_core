package com.coradio.tgfetch.domain.port.`in`

import com.coradio.tgfetch.domain.model.valueobject.SynchronizationSummary

interface SynchronizeTelegramChannelUseCase {
    fun execute(channelId: Long, limit: Int = 50): SynchronizationSummary
}
