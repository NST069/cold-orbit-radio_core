package com.coradio.tgfetch.domain.port.`in`

interface DownloadPendingTrackFilesUseCase {
    fun execute(limit: Int)
}