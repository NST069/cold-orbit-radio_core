package com.coradio.tgfetch.domain.model

import java.time.Instant
import java.util.UUID

data class TelegramPost(
    val id: UUID? = null,
    val channelId: Long,
    val messageId: Long,
    var track: Track,
    var trackFile: TrackFile,
    var rawText: String? = null,
    val publishedAt: Instant,
) {
    fun changeTrack(track: Track, trackFile: TrackFile, rawText: String? = null) {
        this.track = track
        this.trackFile = trackFile
        if (rawText != null) this.rawText = rawText
    }

}
