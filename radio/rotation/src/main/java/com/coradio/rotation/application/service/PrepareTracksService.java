package com.coradio.rotation.application.service;

import com.coradio.rotation.application.dto.TrackInfo;
import com.coradio.rotation.application.exception.FileDownloadingException;
import com.coradio.rotation.application.exception.PlaybackPreparationException;
import com.coradio.rotation.application.exception.TrackNotFoundException;
import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.domain.port.in.PrepareTracksUseCase;
import com.coradio.rotation.domain.port.out.persistence.TrackCatalogPort;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import com.coradio.rotation.domain.port.out.storage.StorageGatewayPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrepareTracksService implements PrepareTracksUseCase {

    private final TrackQueueRepositoryPort trackQueueRepository;

    private final TrackCatalogPort trackCatalogPort;

    private final StorageGatewayPort storageGateway;

    @Override
    public void prepareTracks() {
        List<TrackQueueItem> newQueueItems = trackQueueRepository.findAllByStatus(PlaybackStatus.CREATED);
        if (newQueueItems.isEmpty()) return;

        newQueueItems.forEach(this::prepareTrack);

    }

    public void prepareTrack(TrackQueueItem queueItem) {
        try {
            markDownloading(queueItem.id());
            log.debug("Downloading track: {}", queueItem.trackId());

            TrackInfo track = trackCatalogPort.findById(queueItem.trackId())
                    .orElseThrow(() -> new TrackNotFoundException(queueItem.trackId().toString()));

            String localPath = storageGateway.downloadFile(track.storageKey());
            if(localPath.isBlank()) throw new FileDownloadingException(track.storageKey());

            markReady(queueItem.id(), localPath);
            log.debug("Track is ready: {}", queueItem.trackId());
        } catch (PlaybackPreparationException ex) {
            markFailed(queueItem.id(), ex.getMessage());
            log.error("Failed to prepare track {}", queueItem.trackId().toString(), ex);
        }
    }

    public void markDownloading(UUID id) {
        trackQueueRepository.markDownloading(id);
    }

    public void markFailed(UUID id, String reason) {
        trackQueueRepository.markFailed(id, reason);
    }

    public void markReady(UUID id, String localPath) {
        trackQueueRepository.markReady(id, localPath);
    }

}
