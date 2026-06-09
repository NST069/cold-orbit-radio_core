package com.coradio.tgfetch.infrastructure.out.persistence.repository

import com.coradio.tgfetch.domain.enums.TrackFileStatus
import com.coradio.tgfetch.infrastructure.out.persistence.entity.TrackEntity
import com.coradio.tgfetch.infrastructure.out.persistence.entity.TrackFileEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.Optional
import java.util.UUID

@Repository
interface TrackFileRepository : JpaRepository<TrackFileEntity, UUID> {
    fun findByTrackEntity(trackEntity: TrackEntity): Optional<TrackFileEntity>

    fun findAllByStatus(
        status: TrackFileStatus
    ): List<TrackFileEntity>

    fun existsByTelegramFileUniqueId(
        telegramFileUniqueId: String
    ): Boolean

    @Modifying
    @Transactional
    @Query(
        """
        update TrackFileEntity t
        set t.status = :status
        where t.id = :id
        and t.status = :prevStatus
    """
    )
    fun updateStatus(id: UUID, status: TrackFileStatus, prevStatus: TrackFileStatus): Int

    @Modifying
    @Transactional
    @Query(
        """
        update TrackFileEntity t
        set t.status = "READY",
            t.storageKey = :storageKey
        where t.id = :id
    """
    )
    fun markReady(id: UUID, storageKey: String)

    @Modifying
    @Transactional
    @Query(
        """
        update TrackFileEntity t
        set t.retryCount = t.retryCount + 1,
        t.lastDownloadAttemptAt = CURRENT_TIMESTAMP
        where t.id = :id
    """
    )
    fun incrementRetry(id: UUID)
}
