package com.coradio.rotation.application.service;

import com.coradio.rotation.application.dto.TrackInfo;
import com.coradio.rotation.application.exception.PlaybackPreparationException;
import com.coradio.rotation.application.exception.QueueItemNotFoundException;
import com.coradio.rotation.application.exception.TrackNotFoundException;
import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.domain.port.in.MonitorPlaybackUseCase;
import com.coradio.rotation.domain.port.out.liquidsoap.PlaybackEnginePort;
import com.coradio.rotation.domain.port.out.persistence.PlaybackHistoryRepositoryPort;
import com.coradio.rotation.domain.port.out.persistence.TrackCatalogPort;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonitorPlaybackService implements MonitorPlaybackUseCase {

    private final PlaybackEnginePort playbackEngine;

    private final TrackQueueRepositoryPort trackQueueRepository;

    private final TrackCatalogPort trackCatalogPort;

    private final PlaybackHistoryRepositoryPort playbackHistoryRepository;

    @Override
    public void monitorPlayback() {
        try {
            String currentTrack = playbackEngine.getCurrentTrack().orElse(null);
            if (currentTrack == null) return;

            TrackQueueItem currentQueueItem = trackQueueRepository.findByLocalPath(currentTrack)
                    .orElseThrow(() -> new QueueItemNotFoundException(currentTrack));

            TrackQueueItem playingTrack = trackQueueRepository.findPlayingTrack().orElse(null);

            if (playingTrack == null) {
                markPlaying(currentQueueItem);
                return;
            }

            if (playingTrack.equals(currentQueueItem)) return;

            markPlayed(playingTrack.id());
            markPlaying(currentQueueItem);

            log.debug("Track changed from {} to {}", playingTrack.trackId(), currentQueueItem.trackId());
        } catch (PlaybackPreparationException ex) {
            log.error("Error while monitoring playback", ex);
        }
    }

    private void markPlaying(TrackQueueItem queueItem) {
        trackQueueRepository.markPlaying(queueItem.id());
        TrackInfo track = trackCatalogPort.findById(queueItem.trackId())
                .orElseThrow(() -> new TrackNotFoundException(queueItem.trackId().toString()));
        PlaybackHistoryItem historyItem = new PlaybackHistoryItem(null, queueItem.trackId(), track.artist(), track.title(), Instant.now());
        playbackHistoryRepository.save(historyItem);
    }

    private void markPlayed(UUID id) {
        trackQueueRepository.markPlayed(id);
    }

}
