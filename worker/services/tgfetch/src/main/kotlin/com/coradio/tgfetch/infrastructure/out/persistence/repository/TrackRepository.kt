package com.coradio.tgfetch.infrastructure.out.persistence.repository

import com.coradio.tgfetch.infrastructure.out.persistence.entity.TrackEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface TrackRepository : JpaRepository<TrackEntity, UUID> {
    fun findByTitleAndArtist(title: String, artist: String): Optional<TrackEntity>
}
