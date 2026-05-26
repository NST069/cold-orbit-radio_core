package com.coradio.tgfetch.domain.model

import com.coradio.tgfetch.domain.enums.TrackFileStatus
import java.time.Instant
import java.util.UUID

data class TrackFile(
    val id: UUID? = null,
    val track: Track,
    val etag: String,
    val telegramFileId: String,
    val telegramFileUniqueId: String,
    var storageKey: String? = null,
    val fileSize: Long,
    val mimeType: String,
    var status: TrackFileStatus? = null,
    var retryCount: Int = 0,
    var lastDownloadAttemptAt: Instant? = null,
) {
    fun changeStatus(newStatus: TrackFileStatus) {
        this.status = newStatus
    }

    fun addStorageKey(storageKey: String) {
        this.storageKey = storageKey
    }

    fun retry() {
        this.retryCount = this.retryCount.plus(1)
        this.lastDownloadAttemptAt = Instant.now()
    }
}
