package com.coradio.tgfetch.infrastructure.persistence.adapter

import com.coradio.tgfetch.domain.model.Track
import com.coradio.tgfetch.domain.port.out.persistence.TrackRepositoryPort
import com.coradio.tgfetch.infrastructure.persistence.mapper.TrackMapper
import com.coradio.tgfetch.infrastructure.persistence.repository.TrackRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TrackAdapter(
    private val trackRepository: TrackRepository
): TrackRepositoryPort {

    override fun save(track: Track): Track {
        val entity = TrackMapper.toEntity(track)
        val saved = trackRepository.save(entity)
        return TrackMapper.toDomain(saved)
    }

    override fun findById(id: UUID): Track? {
        return trackRepository.findById(id)
            .map(TrackMapper::toDomain)
            .orElse(null)
    }

    override fun findByTitleAndArtist(
        title: String,
        artist: String
    ): Track? {
        return trackRepository.findByTitleAndArtist(title, artist)
            .map(TrackMapper::toDomain)
            .orElse(null)
    }

    override fun findAll(): List<Track> {
        return trackRepository.findAll()
            .map(TrackMapper::toDomain)
    }

    override fun deleteById(id: UUID) {
        trackRepository.deleteById(id)
    }
}
