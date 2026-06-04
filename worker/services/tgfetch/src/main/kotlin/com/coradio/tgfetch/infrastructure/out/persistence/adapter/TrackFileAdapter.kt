package com.coradio.tgfetch.infrastructure.out.persistence.adapter

import com.coradio.tgfetch.domain.enums.TrackFileStatus
import com.coradio.tgfetch.domain.model.TrackFile
import com.coradio.tgfetch.domain.model.view.TrackFileJobView
import com.coradio.tgfetch.domain.port.out.persistence.TrackFileRepositoryPort
import com.coradio.tgfetch.infrastructure.out.persistence.mapper.TrackFileMapper
import com.coradio.tgfetch.infrastructure.out.persistence.repository.TrackFileRepository
import com.coradio.tgfetch.infrastructure.out.persistence.repository.TrackRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TrackFileAdapter(
    private val trackFileRepository: TrackFileRepository,
    private val trackRepository: TrackRepository
): TrackFileRepositoryPort {
    override fun save(trackFile: TrackFile): TrackFile {
        val entity = TrackFileMapper.toEntity(trackFile)
        val saved = trackFileRepository.save(entity)
        return TrackFileMapper.toDomain(saved)
    }

    override fun findById(id: UUID): TrackFile? {
        return trackFileRepository.findById(id)
            .map(TrackFileMapper::toDomain)
            .orElse(null)
    }

    override fun findByTrackId(trackId: UUID): TrackFile? {
        val trackEntity = trackRepository.findById(trackId)
            .orElseThrow { EntityNotFoundException("Track with id $trackId not found") }
        return trackFileRepository.findByTrackEntity(trackEntity)
            .map(TrackFileMapper::toDomain)
            .orElse(null)
    }

    override fun findAll(): List<TrackFile> {
        return trackFileRepository.findAll()
            .map(TrackFileMapper::toDomain)
    }

    override fun deleteById(id: UUID) {
        trackFileRepository.deleteById(id)
    }

    override fun findAllByStatus(status: TrackFileStatus): List<TrackFileJobView> {
        return trackFileRepository.findAllByStatus(status)
            .map(TrackFileMapper::toJobView)
    }

    override fun existsByTelegramFileUniqueId(telegramFileUniqueId: String): Boolean {
        return trackFileRepository.existsByTelegramFileUniqueId(telegramFileUniqueId)
    }

    override fun updateStatus(id: UUID, status: TrackFileStatus, prevStatus: TrackFileStatus): Int {
        return trackFileRepository.updateStatus(id, status, prevStatus)
    }

    override fun markReady(id: UUID, storageKey: String) {
        trackFileRepository.markReady(id, storageKey)
    }

    override fun incrementRetry(id: UUID) {
        trackFileRepository.incrementRetry(id)
    }

}
