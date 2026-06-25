package com.coradio.rotation.domain.port.out.persistence;

import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.model.TrackQueueItem;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrackQueueRepositoryPort {
    TrackQueueItem save(TrackQueueItem trackQueueItem);

    Optional<TrackQueueItem> findById(UUID id);

    List<TrackQueueItem> findAllByStatus(PlaybackStatus status);

    List<TrackQueueItem> findAll();

    void deleteById(UUID id);

    int deleteAllByStatus(PlaybackStatus status);

    int countQueued();

    List<UUID> findActiveTrackIds();

    void markDownloading(UUID id);

    void markFailed(UUID id, String reason);

    void markQueued(UUID id);

    void markReady(UUID id, String localPath);

    void markPlaying(UUID id);

    void markPlayed(UUID id);

    List<TrackQueueItem> findReadyTracks(int limit);

    Optional<TrackQueueItem> findByLocalPath(String localPath);

    Optional<TrackQueueItem> findPlayingTrack();

    List<TrackQueueItem> findAllForDeletionBefore(Instant threshold);

    List<String> findAllLocalPaths();

    List<TrackQueueItem> findAllQueuedOrPlayingOrderByCreatedAt();

    boolean existsByStatusIn(List<PlaybackStatus> statuses);
}
