package com.coradio.rotation.application.service;

import com.coradio.rotation.application.dto.LocalFileInfo;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.domain.port.in.CleanupUseCase;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import com.coradio.rotation.domain.port.out.storage.LocalStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CleanupService implements CleanupUseCase {

    private final TrackQueueRepositoryPort trackQueueRepository;

    private final LocalStoragePort localStoragePort;

    @Override
    public void cleanupProcessedTracks(Duration retention) {

        log.debug("cleanupProcessedTracks GC Started");
        Instant jobStart = Instant.now();

        Instant threshold = Instant.now().minus(retention);

        List<TrackQueueItem> targets = trackQueueRepository.findAllForDeletionBefore(threshold);

        targets.forEach(target -> {
            try {
                localStoragePort.delete(Path.of(target.localPath()));
                trackQueueRepository.deleteById(target.id());
                log.debug("Deleted item: {}", target.id());
            } catch (Exception e) {
                log.error("Error deleting item {}", target.id(), e);
            }
        });

        log.debug("cleanupProcessedTracks GC Ended in {}s", Duration.between(jobStart, Instant.now()).getSeconds());
    }

    @Override
    public void cleanupOrphanFiles(Duration retention) {

        log.debug("cleanupOrphanFiles GC Started");
        Instant jobStart = Instant.now();

        Instant threshold = Instant.now().minus(retention);

        Set<Path> dbPaths = trackQueueRepository.findAllLocalPaths().stream()
                .map(Path::of)
                .collect(Collectors.toSet());

        List<LocalFileInfo> files = localStoragePort.listAllFiles();

        files.forEach(file -> {
            if (!dbPaths.contains(file.path()) && file.lastModified().isBefore(threshold)) {
                try {
                    localStoragePort.delete(file.path());
                    log.debug("Deleted file: {}", file.path());
                } catch (Exception e) {
                    log.error("Error deleting file {}", file.path(), e);
                }
            }
        });

        log.debug("cleanupOrphanFiles GC Ended in {}s", Duration.between(jobStart, Instant.now()).getSeconds());
    }
}
