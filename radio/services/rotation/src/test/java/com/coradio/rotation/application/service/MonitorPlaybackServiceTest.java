package com.coradio.rotation.application.service;

import com.coradio.rotation.application.dto.TrackInfo;
import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.domain.port.out.liquidsoap.PlaybackEnginePort;
import com.coradio.rotation.domain.port.out.persistence.PlaybackHistoryRepositoryPort;
import com.coradio.rotation.domain.port.out.persistence.TrackCatalogPort;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitorPlaybackServiceTest {

    @Mock
    private PlaybackEnginePort playbackEngine;

    @Mock
    private TrackQueueRepositoryPort trackQueueRepository;

    @Mock
    private TrackCatalogPort trackCatalogPort;

    @Mock
    private PlaybackHistoryRepositoryPort playbackHistoryRepository;

    @InjectMocks
    private MonitorPlaybackService service;

    @Test
    void monitorPlayback_nothingIsPlaying_shouldDoNothing() {
        when(playbackEngine.getCurrentTrack()).thenReturn(Optional.empty());

        service.monitorPlayback();

        verify(trackQueueRepository, never()).findByLocalPath(any());
        verify(trackQueueRepository, never()).findPlayingTrack();
        verify(trackQueueRepository, never()).markPlaying(any());
        verify(trackQueueRepository, never()).markPlayed(any());
        verify(playbackHistoryRepository, never()).save(any());
    }

    @Test
    void monitorPlayback_radioStartedNoPlayingTrackExists_shouldMarkPlaying() {
        UUID trackId = UUID.randomUUID();
        UUID queueId = UUID.randomUUID();
        String path = "/music/a.mp3";
        TrackQueueItem current = new TrackQueueItem(queueId, trackId, PlaybackStatus.QUEUED, path, Instant.now(), null);
        TrackInfo trackInfo = new TrackInfo(trackId, "Artist", "Title", 180, "storage-key");

        when(playbackEngine.getCurrentTrack()).thenReturn(Optional.of(path));
        when(trackQueueRepository.findByLocalPath(path)).thenReturn(Optional.of(current));
        when(trackQueueRepository.findPlayingTrack()).thenReturn(Optional.empty());
        when(trackCatalogPort.findById(trackId)).thenReturn(Optional.of(trackInfo));

        service.monitorPlayback();

        verify(trackQueueRepository).markPlaying(queueId);
        verify(playbackHistoryRepository).save(any(PlaybackHistoryItem.class));
        verify(trackQueueRepository, never()).markPlayed(any());
    }

    @Test
    void monitorPlayback_currentTrackStillPlaying_shouldDoNothing() {
        UUID trackId = UUID.randomUUID();
        UUID queueId = UUID.randomUUID();
        String path = "/music/a.mp3";
        TrackQueueItem current = new TrackQueueItem(queueId, trackId, PlaybackStatus.PLAYING, path, Instant.now(), null);

        when(playbackEngine.getCurrentTrack()).thenReturn(Optional.of(path));
        when(trackQueueRepository.findByLocalPath(path)).thenReturn(Optional.of(current));
        when(trackQueueRepository.findPlayingTrack()).thenReturn(Optional.of(current));

        service.monitorPlayback();

        verify(trackQueueRepository, never()).markPlaying(any());
        verify(trackQueueRepository, never()).markPlayed(any());
        verify(playbackHistoryRepository, never()).save(any());
    }

    @Test
    void monitorPlayback_trackChanged_shouldSwitchTrack() {
        UUID playingTrackId = UUID.randomUUID();
        UUID currentTrackId = UUID.randomUUID();
        UUID playingQueueId = UUID.randomUUID();
        UUID currentQueueId = UUID.randomUUID();
        TrackQueueItem playing = new TrackQueueItem(playingQueueId, playingTrackId, PlaybackStatus.PLAYING, "/music/a.mp3", Instant.now(), null);
        TrackQueueItem current = new TrackQueueItem(currentQueueId, currentTrackId, PlaybackStatus.QUEUED, "/music/b.mp3", Instant.now(), null);
        TrackInfo currentTrackInfo = new TrackInfo(currentTrackId, "Artist", "Title", 180, "storage-key");

        when(playbackEngine.getCurrentTrack()).thenReturn(Optional.of("/music/b.mp3"));
        when(trackQueueRepository.findByLocalPath("/music/b.mp3")).thenReturn(Optional.of(current));
        when(trackQueueRepository.findPlayingTrack()).thenReturn(Optional.of(playing));
        when(trackCatalogPort.findById(currentTrackId)).thenReturn(Optional.of(currentTrackInfo));

        service.monitorPlayback();

        InOrder inOrder = inOrder(trackQueueRepository, playbackHistoryRepository);

        inOrder.verify(trackQueueRepository).markPlayed(playingQueueId);
        inOrder.verify(trackQueueRepository).markPlaying(currentQueueId);
        inOrder.verify(playbackHistoryRepository).save(any(PlaybackHistoryItem.class));
    }

}
