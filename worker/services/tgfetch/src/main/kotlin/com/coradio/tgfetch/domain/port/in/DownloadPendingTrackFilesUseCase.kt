package com.coradio.tgfetch.domain.port.`in`

import com.coradio.tgfetch.domain.model.valueobject.DownloadSummary

interface DownloadPendingTrackFilesUseCase {
    fun execute(): DownloadSummary
}
