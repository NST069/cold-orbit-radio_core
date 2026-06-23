package com.coradio.rotation.application.service;

import com.coradio.rotation.application.dto.LocalFileInfo;
import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import com.coradio.rotation.domain.port.out.storage.LocalStoragePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CleanupServiceTest {

    @Mock
    private TrackQueueRepositoryPort repository;

    @Mock
    private LocalStoragePort storage;

    @InjectMocks
    private CleanupService service;

    @Test
    void cleanupProcessedTracksShouldDeleteFilesAndEntities() {

        TrackQueueItem played = new TrackQueueItem(UUID.randomUUID(), UUID.randomUUID(), PlaybackStatus.READY, "/music/a.mp3", Instant.now().minus(1, ChronoUnit.HOURS), Instant.now());

        TrackQueueItem failed = new TrackQueueItem(UUID.randomUUID(), UUID.randomUUID(), PlaybackStatus.READY, "/music/b.mp3", Instant.now().minus(1, ChronoUnit.HOURS), Instant.now());

        when(repository.findAllForDeletionBefore(any()))
                .thenReturn(List.of(played, failed));

        service.cleanupProcessedTracks(Duration.ofHours(1));

        verify(storage).delete(Path.of("/music/a.mp3"));
        verify(storage).delete(Path.of("/music/b.mp3"));

        verify(repository).deleteById(played.id());
        verify(repository).deleteById(failed.id());
    }

    @Test
    void cleanupProcessedTracksShouldDoNothingWhenNothingFound() {

        when(repository.findAllForDeletionBefore(any()))
                .thenReturn(List.of());

        service.cleanupProcessedTracks(Duration.ofHours(1));

        verifyNoInteractions(storage);

        verify(repository, never()).deleteById(any());
    }

    @Test
    void cleanupProcessedTracksShouldContinueWhenDeleteFails() {

        TrackQueueItem first = new TrackQueueItem(UUID.randomUUID(), UUID.randomUUID(), PlaybackStatus.READY, "/music/a.mp3", Instant.now().minus(1, ChronoUnit.HOURS), Instant.now());

        TrackQueueItem second = new TrackQueueItem(UUID.randomUUID(), UUID.randomUUID(), PlaybackStatus.READY, "/music/b.mp3", Instant.now().minus(1, ChronoUnit.HOURS), Instant.now());

        when(repository.findAllForDeletionBefore(any()))
                .thenReturn(List.of(first, second));

        doThrow(new RuntimeException())
                .when(storage)
                .delete(Path.of("/music/a.mp3"));

        service.cleanupProcessedTracks(Duration.ofHours(1));

        verify(storage).delete(Path.of("/music/a.mp3"));
        verify(storage).delete(Path.of("/music/b.mp3"));

        verify(repository).deleteById(second.id());

        verify(repository, never()).deleteById(first.id());
    }

    @Test
    void cleanupOrphanFilesShouldDeleteOldOrphans() {

        LocalFileInfo orphan =
                new LocalFileInfo(
                        Path.of("/music/a.mp3"),
                        Instant.now().minus(Duration.ofHours(2))
                );

        when(storage.listAllFiles())
                .thenReturn(List.of(orphan));

        when(repository.findAllLocalPaths())
                .thenReturn(List.of());

        service.cleanupOrphanFiles(Duration.ofHours(1));

        verify(storage).delete(Path.of("/music/a.mp3"));
    }

    @Test
    void cleanupOrphanFilesShouldNotDeleteFreshFiles() {

        LocalFileInfo orphan =
                new LocalFileInfo(
                        Path.of("/music/a.mp3"),
                        Instant.now()
                );

        when(storage.listAllFiles())
                .thenReturn(List.of(orphan));

        when(repository.findAllLocalPaths())
                .thenReturn(List.of());

        service.cleanupOrphanFiles(Duration.ofHours(1));

        verify(storage, never()).delete(any());
    }

    @Test
    void cleanupOrphanFilesShouldSkipReferencedFiles() {

        LocalFileInfo file =
                new LocalFileInfo(
                        Path.of("/music/a.mp3"),
                        Instant.now().minus(Duration.ofHours(2))
                );

        when(storage.listAllFiles())
                .thenReturn(List.of(file));

        when(repository.findAllLocalPaths())
                .thenReturn(List.of("/music/a.mp3"));

        service.cleanupOrphanFiles(Duration.ofHours(1));

        verify(storage, never()).delete(any());
    }

}