package com.coradio.tgfetch.domain.model

import com.coradio.tgfetch.application.util.MetadataResolver
import com.coradio.tgfetch.domain.port.out.telegram.TelegramTrackData
import java.util.UUID

data class Track (
    val id : UUID ?= null,
    var title : String,
    var artist: String,
    var duration: Int,
    var trackFile: TrackFile? = null,
    var telegramPost: TelegramPost? = null,
) {
    fun attachTrackFile(file: TrackFile) {
        this.trackFile = file
    }

    fun attachTelegramPost(post: TelegramPost) {
        this.telegramPost = post
    }

    fun syncWith(post: TelegramTrackData): Boolean {
        var changed = false

        changed = this.trackFile?.syncWith(post) ?: false || changed
        changed = this.telegramPost?.syncWith(post) ?: false || changed

        val metadataResolver = MetadataResolver()
        val metadata = metadataResolver.resolve(post)

        if (this.title != metadata.title) {
            this.title = metadata.title
            this.trackFile?.markPending()
            changed = true
        }

        if (this.artist != metadata.artist) {
            this.artist = metadata.artist
            this.trackFile?.markPending()
            changed = true
        }

        if (post.durationSeconds != null && this.duration != post.durationSeconds) {
            this.duration = post.durationSeconds
            changed = true
        }

        return changed
    }

}
