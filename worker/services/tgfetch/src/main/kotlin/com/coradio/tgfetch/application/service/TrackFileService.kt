package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.model.TrackFile
import com.coradio.tgfetch.domain.port.out.TrackFileRepositoryPort
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TrackFileService(
    private val trackFileRepository: TrackFileRepositoryPort,
) {
    fun saveTrackFile(trackFile: TrackFile): TrackFile = trackFileRepository.save(trackFile)

    fun findAllTrackFiles(): List<TrackFile> = trackFileRepository.findAll()

    fun findTrackFileById(id: UUID): TrackFile? = trackFileRepository.findById(id)

    fun findByTrackId(trackId: UUID): TrackFile? = trackFileRepository.findByTrackId(trackId)

    fun deleteById(id: UUID) = trackFileRepository.deleteById(id)

}
