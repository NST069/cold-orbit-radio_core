package com.coradio.tgfetch.domain.model

import com.coradio.tgfetch.domain.enums.TrackFileStatus
import com.coradio.tgfetch.domain.port.out.telegram.TelegramTrackData
import java.time.Instant
import java.util.UUID

data class TrackFile(
    val id: UUID? = null,
    val etag: String,
    var telegramFileId: String,
    var telegramFileUniqueId: String,
    var storageKey: String? = null,
    var fileName: String,
    var fileSize: Long,
    var mimeType: String,
    var status: TrackFileStatus? = null,
    var retryCount: Int = 0,
    var lastDownloadAttemptAt: Instant? = null,
) {
    fun changeStatus(newStatus: TrackFileStatus) {
        this.status = newStatus
    }

    fun markPending() {
        this.status = TrackFileStatus.PENDING
        this.retryCount = 0
    }

    fun syncWith(post: TelegramTrackData): Boolean {
        var changed = false

        if (this.telegramFileId != post.remoteFileId) {
            this.telegramFileId = post.remoteFileId
            changed = true
        }

        if (this.telegramFileUniqueId != post.uniqueFileId) {
            this.telegramFileUniqueId = post.uniqueFileId
            changed = true
        }

        if (post.fileName != null && this.fileName != post.fileName) {
            this.fileName = post.fileName
            changed = true
        }

        if (post.fileSizeBytes != null && this.fileSize != post.fileSizeBytes) {
            this.fileSize = post.fileSizeBytes
            changed = true
        }

        if (post.mimeType != null && this.mimeType != post.mimeType) {
            this.mimeType = post.mimeType
            changed = true
        }

        if (changed) {
            this.changeStatus(TrackFileStatus.PENDING)
        }

        return changed
    }

}
