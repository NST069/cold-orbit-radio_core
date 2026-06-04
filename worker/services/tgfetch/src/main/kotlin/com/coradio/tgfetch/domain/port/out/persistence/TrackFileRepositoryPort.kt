package com.coradio.tgfetch.domain.port.out.persistence

import com.coradio.tgfetch.domain.enums.TrackFileStatus
import com.coradio.tgfetch.domain.model.TrackFile
import com.coradio.tgfetch.domain.model.view.TrackFileJobView
import java.util.UUID

interface TrackFileRepositoryPort {
    fun save(trackFile: TrackFile): TrackFile
    fun findById(id: UUID): TrackFile?
    fun findByTrackId(trackId: UUID): TrackFile?
    fun findAll(): List<TrackFile>
    fun deleteById(id: UUID)

    fun findAllByStatus(status: TrackFileStatus): List<TrackFileJobView>

    fun existsByTelegramFileUniqueId(telegramFileUniqueId: String): Boolean

    fun updateStatus(id: UUID, status: TrackFileStatus, prevStatus: TrackFileStatus): Int
    fun markReady(id: UUID, storageKey: String)
    fun incrementRetry(id: UUID)
}
