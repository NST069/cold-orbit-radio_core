package com.coradio.tgfetch.domain.port.out.persistence

import com.coradio.tgfetch.domain.model.TrackFile
import java.util.UUID

interface TrackFileRepositoryPort {
    fun save(trackFile: TrackFile): TrackFile
    fun findById(id: UUID): TrackFile?
    fun findByTrackId(trackId: UUID): TrackFile?
    fun findAll(): List<TrackFile>
    fun deleteById(id: UUID)
}
