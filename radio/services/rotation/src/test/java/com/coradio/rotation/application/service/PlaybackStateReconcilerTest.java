package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaybackStateReconcilerTest {

    @Mock
    private TrackQueueRepositoryPort repository;

    @InjectMocks
    private PlaybackStateReconciler reconciler;

    @Test
    void shouldRecoverEmptyEngine() {

        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();

        List<TrackQueueItem> tracks = List.of(
                new TrackQueueItem(a, UUID.randomUUID(), PlaybackStatus.PLAYING, "/music/a.mp3", Instant.now(), null),
                new TrackQueueItem(b, UUID.randomUUID(), PlaybackStatus.QUEUED, "/music/b.mp3", Instant.now(), null),
                new TrackQueueItem(c, UUID.randomUUID(), PlaybackStatus.QUEUED, "/music/c.mp3", Instant.now(), null)
        );

        when(repository.findAllQueuedOrPlayingOrderByCreatedAt())
                .thenReturn(tracks);

        reconciler.reconcile(Optional.empty(), 0);

        verify(repository).markReady(a, "/music/a.mp3");
        verify(repository).markReady(b, "/music/b.mp3");
        verify(repository).markReady(c, "/music/c.mp3");
    }

    @Test
    void shouldRecoverFromCurrentTrack() {

        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        UUID d = UUID.randomUUID();

        List<TrackQueueItem> tracks = List.of(
                new TrackQueueItem(a, UUID.randomUUID(), PlaybackStatus.PLAYING, "/music/a.mp3", Instant.now(), null),
                new TrackQueueItem(b, UUID.randomUUID(), PlaybackStatus.QUEUED, "/music/b.mp3", Instant.now(), null),
                new TrackQueueItem(c, UUID.randomUUID(), PlaybackStatus.QUEUED, "/music/c.mp3", Instant.now(), null),
                new TrackQueueItem(d, UUID.randomUUID(), PlaybackStatus.QUEUED, "/music/D.mp3", Instant.now(), null)
        );

        when(repository.findAllQueuedOrPlayingOrderByCreatedAt()).thenReturn(tracks);

        reconciler.reconcile(Optional.of("/music/c.mp3"), 4);

        verify(repository).markPlayed(a);
        verify(repository).markPlayed(b);
        verify(repository).markPlaying(c);
        verify(repository).markQueued(d);
    }

    @Test
    void shouldKeepCurrentPlayingTrack() {

        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();

        List<TrackQueueItem> tracks = List.of(
                new TrackQueueItem(a, UUID.randomUUID(), PlaybackStatus.PLAYING, "/music/a.mp3", Instant.now(), null),
                new TrackQueueItem(b, UUID.randomUUID(), PlaybackStatus.QUEUED, "/music/b.mp3", Instant.now(), null)
        );

        when(repository.findAllQueuedOrPlayingOrderByCreatedAt()).thenReturn(tracks);

        reconciler.reconcile(Optional.of("/music/a.mp3"), 2);

        verify(repository).markPlaying(a);
        verify(repository).markQueued(b);
        verify(repository, never()).markReady(any(), any());
    }

    @Test
    void shouldResetQueueWhenCurrentTrackUnknown() {

        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();

        List<TrackQueueItem> tracks = List.of(
                new TrackQueueItem(a, UUID.randomUUID(), PlaybackStatus.PLAYING, "/music/a.mp3", Instant.now(), null),
                new TrackQueueItem(b, UUID.randomUUID(), PlaybackStatus.QUEUED, "/music/b.mp3", Instant.now(), null)
        );

        when(repository.findAllQueuedOrPlayingOrderByCreatedAt()).thenReturn(tracks);

        reconciler.reconcile(Optional.of("/music/x.mp3"), 2);

        verify(repository).markReady(a, "/music/a.mp3");
        verify(repository).markReady(b, "/music/b.mp3");
    }

    @Test
    void shouldDoNothingWhenNoTracks() {

        when(repository.findAllQueuedOrPlayingOrderByCreatedAt())
                .thenReturn(List.of());

        reconciler.reconcile(Optional.empty(), 0);

        verify(repository, never()).markReady(any(), any());
        verify(repository, never()).markQueued(any());
        verify(repository, never()).markPlaying(any());
        verify(repository, never()).markPlayed(any());
    }

}
