package com.coradio.tgfetch.infrastructure.persistence.mapper

import com.coradio.tgfetch.domain.model.Track
import com.coradio.tgfetch.infrastructure.persistence.entity.TrackEntity

object TrackMapper {
    fun toDomain(trackEntity: TrackEntity): Track = Track(
        id = trackEntity.id,
        title = trackEntity.title,
        artist = trackEntity.artist,
        duration = trackEntity.duration,
    )

    fun toEntity(track: Track): TrackEntity = TrackEntity().apply {
        this.id = track.id
        this.title = if (track.title.trim().isEmpty()) "<Без названия>" else track.title
        this.artist = if (track.artist.trim().isEmpty()) "<Неизвестен>" else track.artist
        this.duration = track.duration
    }
}
