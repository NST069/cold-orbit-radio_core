package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import com.coradio.rotation.infrastructure.out.persistense.adapter.TrackQueueAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaybackStateReconciler {

    private final TrackQueueRepositoryPort repository;

    @Transactional
    public void reconcile(Optional<String> currentTrack, int queueLength) {

        List<TrackQueueItem> tracks = repository.findAllQueuedOrPlayingOrderByCreatedAt();

        if (tracks.isEmpty()) return;

        log.debug("[Reconciler] Processing {} tracks", tracks.size());

        if (currentTrack.isEmpty() && queueLength == 0) {
            log.debug("Full recovery");
            recoverEmptyEngine(tracks);
            return;
        }

        if (currentTrack.isEmpty()) {
            recoverInconsistentState(currentTrack, queueLength);
            return;
        }

        String current = currentTrack.get();

        int currentIndex = findTrackIndex(current, tracks);

        if (currentIndex < 0) {
            recoverUnknownCurrentTrack(current, tracks);
            return;
        }

        log.debug("Applying changes");
        applyCurrentTrackState(currentIndex, tracks);
    }

    private void recoverEmptyEngine(List<TrackQueueItem> tracks) {
        tracks.forEach(track -> repository.markReady(track.id(), track.localPath()));
    }

    private int findTrackIndex(String currentTrack, List<TrackQueueItem> tracks) {
        return IntStream.range(0, tracks.size())
                .filter(i ->
                        tracks.get(i)
                                .localPath()
                                .equals(currentTrack)
                )
                .findFirst()
                .orElse(-1);
    }

    private void applyCurrentTrackState(int currentIndex, List<TrackQueueItem> tracks) {

        for (int i = 0; i < currentIndex; i++) {
            repository.markPlayed(tracks.get(i).id());
        }

        repository.markPlaying(tracks.get(currentIndex).id());

        for (int i = currentIndex + 1; i < tracks.size(); i++) {
            repository.markQueued(tracks.get(i).id());
        }
    }

    private void recoverUnknownCurrentTrack(String currentTrack, List<TrackQueueItem> tracks) {
        log.warn("Current track '{}' not found in database queue", currentTrack);

        recoverEmptyEngine(tracks);
    }

    private void recoverInconsistentState(Optional<String> currentTrack, int queueLength) {

        log.warn("Inconsistent playback state: currentTrack={}, queueLength={}", currentTrack, queueLength);
    }

}
