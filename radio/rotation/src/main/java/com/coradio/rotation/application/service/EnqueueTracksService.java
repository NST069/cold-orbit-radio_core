package com.coradio.rotation.application.service;

import com.coradio.rotation.application.config.PlaybackProperties;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.domain.port.in.EnqueueTracksUseCase;
import com.coradio.rotation.domain.port.out.liquidsoap.PlaybackEnginePort;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EnqueueTracksService implements EnqueueTracksUseCase {

    private final TrackQueueRepositoryPort trackQueueRepository;

    private final PlaybackEnginePort playbackEngine;

    private final PlaybackProperties properties;

    @Override
    public void enqueueTracks() {
        int queueLength = playbackEngine.getQueueLength();
        if (queueLength >= properties.targetSize()) return;
        if (queueLength <= properties.minSize())
            log.warn("Critical queue starving, queue length = {}", queueLength);

        int requiredTracks = properties.targetSize() - queueLength;

        List<TrackQueueItem> queueItems = trackQueueRepository.findReadyTracks(requiredTracks);
        if (queueItems.size() < requiredTracks)
            log.warn("Not enough READY tracks. Required {}, found {}", requiredTracks, queueItems.size());

        queueItems.forEach(queueItem -> {
            try {
                playbackEngine.enqueue(queueItem.localPath());
                markQueued(queueItem.id());
                log.debug("Track put to Liquidsoap queue: {}", queueItem.trackId());
            } catch (Exception ex) {
                markFailed(queueItem.id(), ex.getMessage());
                log.error("Failed to put track({}) to Liquidsoap queue", queueItem.trackId(), ex);
            }
        });
    }

    public void markQueued(UUID id) {
        trackQueueRepository.markQueued(id);
    }

    public void markFailed(UUID id, String reason) {
        trackQueueRepository.markFailed(id, reason);
    }

}
