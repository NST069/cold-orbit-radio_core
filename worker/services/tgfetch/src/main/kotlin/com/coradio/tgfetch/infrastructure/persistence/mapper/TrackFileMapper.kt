package com.coradio.tgfetch.infrastructure.persistence.mapper

import com.coradio.tgfetch.domain.model.TrackFile
import com.coradio.tgfetch.infrastructure.persistence.entity.TrackFileEntity

object TrackFileMapper {
    fun toDomain(trackFileEntity: TrackFileEntity): TrackFile = TrackFile(
        id = trackFileEntity.id,
        track = TrackMapper.toDomain(trackFileEntity.trackEntity),
        sha256 = trackFileEntity.sha256,
        telegramFileUniqueId = trackFileEntity.telegramFileUniqueId,
        storageKey = trackFileEntity.storageKey,
        fileSize = trackFileEntity.fileSize,
        mimeType = trackFileEntity.mimeType
    )

    fun toEntity(trackFile: TrackFile): TrackFileEntity = TrackFileEntity().apply {
        this.id = trackFile.id
        this.trackEntity = TrackMapper.toEntity(trackFile.track)
        this.sha256 = trackFile.sha256
        this.telegramFileUniqueId = trackFile.telegramFileUniqueId
        this.storageKey = trackFile.storageKey
        this.fileSize = trackFile.fileSize
        this.mimeType = trackFile.mimeType
    }
}
