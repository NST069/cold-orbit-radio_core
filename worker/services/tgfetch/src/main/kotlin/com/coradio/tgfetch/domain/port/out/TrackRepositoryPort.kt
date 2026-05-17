package com.coradio.tgfetch.domain.port.out

import com.coradio.tgfetch.domain.model.Track
import java.util.UUID

interface TrackRepositoryPort {
    fun save(track: Track): Track
    fun findById(id: UUID): Track?
    fun findByTitleAndArtist(title: String, artist: String): Track?
    fun findAll(): List<Track>
    fun deleteById(id: UUID)
}
