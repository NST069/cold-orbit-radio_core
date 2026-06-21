package com.coradio.rotation.infrastructure.out.persistense.repository;

import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.infrastructure.out.persistense.entity.TrackQueueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrackQueueRepository extends JpaRepository<TrackQueueEntity, UUID> {

    int deleteAllByStatus(PlaybackStatus status);

    List<TrackQueueEntity> findAllByStatus(PlaybackStatus status);

    int countByStatus(PlaybackStatus playbackStatus);

    @Modifying
    @Transactional
    @Query(
            """
            update TrackQueueEntity t
            set t.status = com.coradio.rotation.domain.enums.PlaybackStatus.DOWNLOADING
            where t.id = :id
        """
    )
    void markDownloading(UUID id);

    @Modifying
    @Transactional
    @Query(
            """
            update TrackQueueEntity t
            set t.status = com.coradio.rotation.domain.enums.PlaybackStatus.FAILED, t.lastError = :reason
            where t.id = :id
        """
    )
    void markFailed(UUID id, String reason);

    @Modifying
    @Transactional
    @Query(
            """
            update TrackQueueEntity t
            set t.status = com.coradio.rotation.domain.enums.PlaybackStatus.QUEUED
            where t.id = :id
        """
    )
    void markQueued(UUID id);

    @Modifying
    @Transactional
    @Query(
            """
            update TrackQueueEntity t
            set t.status = com.coradio.rotation.domain.enums.PlaybackStatus.READY, t.localPath = :localPath
            where t.id = :id
        """
    )
    void markReady(UUID id, String localPath);

    @Modifying
    @Transactional
    @Query(
            """
            update TrackQueueEntity t
            set t.status = com.coradio.rotation.domain.enums.PlaybackStatus.PLAYING
            where t.id = :id
        """
    )
    void markPlaying(UUID id);

    @Modifying
    @Transactional
    @Query(
            """
            update TrackQueueEntity t
            set t.status = com.coradio.rotation.domain.enums.PlaybackStatus.PLAYED
            where t.id = :id
        """
    )
    void markPlayed(UUID id);

    List<TrackQueueEntity> findByStatusOrderByCreatedAtAsc(PlaybackStatus status);

    Optional<TrackQueueItem> findByLocalPath(String localPath);

    Optional<TrackQueueItem> findByStatus(PlaybackStatus status);
}
