package com.coradio.rotation.domain.port.out.persistence;

import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.model.TrackQueueItem;
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
}
