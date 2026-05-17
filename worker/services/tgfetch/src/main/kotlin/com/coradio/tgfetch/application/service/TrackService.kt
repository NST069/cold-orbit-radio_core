package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.model.Track
import com.coradio.tgfetch.domain.port.out.TrackRepositoryPort
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TrackService(
    private val trackRepository: TrackRepositoryPort,
) {
    fun addTrack(track: Track) = trackRepository.save(track)

    fun getTrackById(id: UUID): Track? = trackRepository.findById(id)

    fun getTrackByTitleAndArtist(title: String, artist: String): Track? =
        trackRepository.findByTitleAndArtist(title, artist)

    fun getAllTracks(): List<Track> = trackRepository.findAll()

    fun deleteById(id: UUID) = trackRepository.deleteById(id)

}
