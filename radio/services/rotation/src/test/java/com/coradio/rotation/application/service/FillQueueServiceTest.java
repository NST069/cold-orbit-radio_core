package com.coradio.rotation.application.service;

import com.coradio.rotation.application.config.QueueProperties;
import com.coradio.rotation.application.dto.TrackInfo;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.domain.port.out.dj.TrackSelectionStrategy;
import com.coradio.rotation.domain.port.out.persistence.PlaybackHistoryRepositoryPort;
import com.coradio.rotation.domain.port.out.persistence.TrackCatalogPort;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FillQueueServiceTest {

    @Mock
    private TrackQueueRepositoryPort trackQueueRepository;

    @Mock
    private TrackCatalogPort trackCatalogPort;

    @Mock
    private PlaybackHistoryRepositoryPort playbackHistoryRepository;

    @Mock
    private TrackSelectionStrategy trackSelectionStrategy;

    @Mock
    private QueueProperties properties;

    @InjectMocks
    private FillQueueService service;

    @Test
    void fillQueue_queueIsFull_shouldDoNothing() {
        when(trackQueueRepository.countQueued()).thenReturn(10);
        when(properties.targetSize()).thenReturn(10);

        service.fillQueue();

        verify(trackCatalogPort, never()).findPlayableTracks();
        verify(trackSelectionStrategy, never()).selectTracks(any(), anyInt(), any());
        verify(trackQueueRepository, never()).save(any());
    }

    @Test
    void fillQueue_tracksRequired_shouldFillQueue() {
        TrackInfo track1 = new TrackInfo(UUID.randomUUID(), "Artist 1", "Title 1", 180, "track1.mp3");
        TrackInfo track2 = new TrackInfo(UUID.randomUUID(), "Artist 2", "Title 2", 200, "track2.mp3");

        when(trackQueueRepository.countQueued()).thenReturn(3);
        when(properties.targetSize()).thenReturn(5);
        when(properties.historyHours()).thenReturn(24);
        when(trackCatalogPort.findPlayableTracks()).thenReturn(new ArrayList<>(List.of(track1, track2)));
        when(trackQueueRepository.findActiveTrackIds()).thenReturn(List.of());
        when(playbackHistoryRepository.findAllInRange(24)).thenReturn(List.of());
        when(trackSelectionStrategy.selectTracks(anyList(), eq(2), anyList())).thenReturn(List.of(track1, track2));

        service.fillQueue();

        verify(trackQueueRepository, times(2)).save(any(TrackQueueItem.class));
    }

    @Test
    void fillQueue_tracksAlreadyInQueue_shouldSkipRepeatingTracks() {
        TrackInfo trackA = new TrackInfo(UUID.randomUUID(), "A", "A", 180, "a.mp3");
        TrackInfo trackB = new TrackInfo(UUID.randomUUID(), "B", "B", 180, "b.mp3");
        TrackInfo trackC = new TrackInfo(UUID.randomUUID(), "C", "C", 180, "c.mp3");

        when(trackQueueRepository.countQueued()).thenReturn(0);
        when(properties.targetSize()).thenReturn(5);
        when(properties.historyHours()).thenReturn(24);
        when(trackCatalogPort.findPlayableTracks()).thenReturn(new ArrayList<>(List.of(trackA, trackB, trackC)));
        when(trackQueueRepository.findActiveTrackIds()).thenReturn(List.of(trackB.id()));
        when(playbackHistoryRepository.findAllInRange(24)).thenReturn(List.of());
        when(trackSelectionStrategy.selectTracks(anyList(), anyInt(), anyList())).thenReturn(List.of());

        service.fillQueue();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TrackInfo>> captor = ArgumentCaptor.forClass(List.class);

        verify(trackSelectionStrategy).selectTracks(captor.capture(), eq(5), anyList());

        List<TrackInfo> candidates = captor.getValue();

        assertEquals(2, candidates.size());
        assertTrue(candidates.contains(trackA));
        assertTrue(candidates.contains(trackC));
        assertFalse(candidates.contains(trackB));
    }

    @Test
    void fillQueue_strategyReturnsEmptyList_shouldDoNothing() {
        TrackInfo track = new TrackInfo(UUID.randomUUID(), "Artist", "Title", 180, "track.mp3");

        when(trackQueueRepository.countQueued()).thenReturn(0);
        when(properties.targetSize()).thenReturn(5);
        when(properties.historyHours()).thenReturn(24);
        when(trackCatalogPort.findPlayableTracks()).thenReturn(new ArrayList<>(List.of(track)));
        when(trackQueueRepository.findActiveTrackIds()).thenReturn(List.of());
        when(playbackHistoryRepository.findAllInRange(24)).thenReturn(List.of());
        when(trackSelectionStrategy.selectTracks(anyList(), anyInt(), anyList())).thenReturn(List.of());

        service.fillQueue();

        verify(trackQueueRepository, never()).save(any());
    }
}
