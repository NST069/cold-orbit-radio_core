package com.coradio.tgfetch.infrastructure.persistence.mapper

import com.coradio.tgfetch.domain.enums.TrackFileStatus
import com.coradio.tgfetch.domain.model.TrackFile
import com.coradio.tgfetch.infrastructure.persistence.entity.TrackFileEntity

object TrackFileMapper {
    fun toDomain(trackFileEntity: TrackFileEntity): TrackFile = TrackFile(
        id = trackFileEntity.id,
        track = TrackMapper.toDomain(trackFileEntity.trackEntity),
        etag = trackFileEntity.etag,
        telegramFileId = trackFileEntity.telegramFileId,
        telegramFileUniqueId = trackFileEntity.telegramFileUniqueId,
        storageKey = trackFileEntity.storageKey,
        fileSize = trackFileEntity.fileSize,
        mimeType = trackFileEntity.mimeType,
        status = trackFileEntity.status,
        retryCount = trackFileEntity.retryCount,
        lastDownloadAttemptAt = trackFileEntity.lastDownloadAttemptAt,
    )

    fun toEntity(trackFile: TrackFile): TrackFileEntity = TrackFileEntity().apply {
        this.id = trackFile.id
        this.trackEntity = TrackMapper.toEntity(trackFile.track)
        this.etag = trackFile.etag
        this.telegramFileId = trackFile.telegramFileId
        this.telegramFileUniqueId = trackFile.telegramFileUniqueId
        this.storageKey = trackFile.storageKey
        this.fileSize = trackFile.fileSize
        this.mimeType = trackFile.mimeType
        this.status = trackFile.status ?: TrackFileStatus.CREATED
        this.retryCount = trackFile.retryCount
        this.lastDownloadAttemptAt = trackFile.lastDownloadAttemptAt
    }
}
