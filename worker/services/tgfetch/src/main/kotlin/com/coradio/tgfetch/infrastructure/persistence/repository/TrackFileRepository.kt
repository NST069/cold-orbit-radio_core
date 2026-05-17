package com.coradio.tgfetch.infrastructure.persistence.repository

import com.coradio.tgfetch.infrastructure.persistence.entity.TrackEntity
import com.coradio.tgfetch.infrastructure.persistence.entity.TrackFileEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface TrackFileRepository : JpaRepository<TrackFileEntity, UUID> {
    fun findByTrackEntity(trackEntity: TrackEntity): Optional<TrackFileEntity>
}
