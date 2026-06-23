package com.coradio.rotation.application.service;

import com.coradio.rotation.application.dto.TrackInfo;
import com.coradio.rotation.application.config.QueueProperties;
import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.domain.port.in.FillQueueUseCase;
import com.coradio.rotation.domain.port.out.dj.TrackSelectionStrategy;
import com.coradio.rotation.domain.port.out.persistence.PlaybackHistoryRepositoryPort;
import com.coradio.rotation.domain.port.out.persistence.TrackCatalogPort;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FillQueueService implements FillQueueUseCase {

    private final QueueProperties properties;

    private final TrackQueueRepositoryPort trackQueueRepository;

    private final TrackCatalogPort trackCatalogPort;

    private final TrackSelectionStrategy trackSelectionStrategy;

    private final PlaybackHistoryRepositoryPort playbackHistoryRepository;

    @Override
    public void fillQueue() {
        int queueSize = trackQueueRepository.countQueued();
        if (queueSize >= properties.targetSize()) return;

        int requiredTracks = properties.targetSize() - queueSize;

        List<TrackInfo> candidates = trackCatalogPort.findPlayableTracks();

        List<UUID> queuedTrackIds = trackQueueRepository.findActiveTrackIds();
        candidates.removeIf(track -> queuedTrackIds.contains(track.id()));

        List<PlaybackHistoryItem> history = playbackHistoryRepository.findAllInRange(properties.historyHours());

        List<TrackInfo> selected = trackSelectionStrategy.selectTracks(candidates, requiredTracks, history);

        selected.stream()
                .map(track -> new TrackQueueItem(null, track.id(), PlaybackStatus.CREATED, null, Instant.now(), null))
                .forEach(trackQueueRepository::save);
        log.debug("Added {} tracks to queue", selected.size());
    }

}
