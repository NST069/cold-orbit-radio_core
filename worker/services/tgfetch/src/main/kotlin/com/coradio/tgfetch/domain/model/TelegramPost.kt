package com.coradio.tgfetch.domain.model

import com.coradio.tgfetch.domain.port.out.telegram.TelegramTrackData
import java.time.Instant
import java.util.UUID

data class TelegramPost(
    val id: UUID? = null,
    val channelId: Long,
    val messageId: Long,
    val trackId: UUID? = null,
    var rawText: String? = null,
    val publishedAt: Instant,
){
    fun syncWith(post: TelegramTrackData): Boolean{
        var changed = false

        if(this.rawText != post.rawText){
            this.rawText = post.rawText
            changed = true
        }

        return changed
    }

}
