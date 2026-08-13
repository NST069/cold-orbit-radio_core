package com.coradio.tgfetch.infrastructure.out.persistence.mapper

import com.coradio.tgfetch.domain.enums.TrackFileStatus
import com.coradio.tgfetch.domain.model.TrackFile
import com.coradio.tgfetch.domain.model.view.TrackFileJobView
import com.coradio.tgfetch.infrastructure.out.persistence.entity.TrackFileEntity

object TrackFileMapper {
    fun toDomain(trackFileEntity: TrackFileEntity): TrackFile = TrackFile(
        id = trackFileEntity.id,
        etag = trackFileEntity.etag,
        telegramFileId = trackFileEntity.telegramFileId,
        telegramFileUniqueId = trackFileEntity.telegramFileUniqueId,
        storageKey = trackFileEntity.storageKey,
        fileName = trackFileEntity.fileName,
        fileSize = trackFileEntity.fileSize,
        mimeType = trackFileEntity.mimeType,
        status = trackFileEntity.status,
        retryCount = trackFileEntity.retryCount,
        lastDownloadAttemptAt = trackFileEntity.lastDownloadAttemptAt,
    )

    fun toJobView(trackFileEntity: TrackFileEntity): TrackFileJobView = TrackFileJobView(
        id = trackFileEntity.id,
        telegramFileId = trackFileEntity.telegramFileId,
        artist = trackFileEntity.trackEntity.artist,
        title = trackFileEntity.trackEntity.title,
        retryCount = trackFileEntity.retryCount,
        mimeType = trackFileEntity.mimeType,
        fileName = trackFileEntity.fileName,
    )

    fun toEntity(trackFile: TrackFile): TrackFileEntity = TrackFileEntity().apply {
        this.id = trackFile.id
        this.etag = trackFile.etag
        this.telegramFileId = trackFile.telegramFileId
        this.telegramFileUniqueId = trackFile.telegramFileUniqueId
        this.storageKey = trackFile.storageKey
        this.fileName = trackFile.fileName
        this.fileSize = trackFile.fileSize
        this.mimeType = trackFile.mimeType
        this.status = trackFile.status ?: TrackFileStatus.CREATED
        this.retryCount = trackFile.retryCount
        this.lastDownloadAttemptAt = trackFile.lastDownloadAttemptAt
    }
}
