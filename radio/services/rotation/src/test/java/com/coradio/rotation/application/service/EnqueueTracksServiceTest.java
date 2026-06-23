package com.coradio.rotation.application.service;

import com.coradio.rotation.application.config.PlaybackProperties;
import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.domain.port.out.liquidsoap.PlaybackEnginePort;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnqueueTracksServiceTest {

    @Mock
    private TrackQueueRepositoryPort trackQueueRepository;

    @Mock
    private PlaybackEnginePort playbackEngine;

    @Mock
    private PlaybackProperties properties;

    @InjectMocks
    private EnqueueTracksService service;

    @Test
    void enqueueTracks_liquidsoapQueueIsFull_shouldDoNothing() {
        when(playbackEngine.getQueueLength()).thenReturn(10);
        when(properties.targetSize()).thenReturn(10);

        service.enqueueTracks();

        verify(trackQueueRepository, never()).findReadyTracks(anyInt());
        verify(playbackEngine, never()).enqueue(anyString());
    }

    @Test
    void enqueueTracks_needTracks_shouldEnqueueTracks() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        TrackQueueItem first = new TrackQueueItem(firstId, UUID.randomUUID(), PlaybackStatus.READY, "/music/1.mp3", Instant.now(), null);
        TrackQueueItem second = new TrackQueueItem(secondId, UUID.randomUUID(), PlaybackStatus.READY, "/music/2.mp3", Instant.now(), null);

        when(playbackEngine.getQueueLength()).thenReturn(8);
        when(properties.targetSize()).thenReturn(10);
        when(properties.minSize()).thenReturn(2);
        when(trackQueueRepository.findReadyTracks(2)).thenReturn(List.of(first, second));

        service.enqueueTracks();

        verify(playbackEngine).enqueue("/music/1.mp3");
        verify(playbackEngine).enqueue("/music/2.mp3");
        verify(trackQueueRepository).markQueued(firstId);
        verify(trackQueueRepository).markQueued(secondId);
        verify(trackQueueRepository, never()).markFailed(any(), any());
    }

    @Test
    void enqueueTracks_enqueueFailed_shouldMarkFailed() {
        UUID queueId = UUID.randomUUID();
        TrackQueueItem item = new TrackQueueItem(queueId, UUID.randomUUID(), PlaybackStatus.READY, "/music/1.mp3", Instant.now(), null);

        when(playbackEngine.getQueueLength()).thenReturn(0);
        when(properties.targetSize()).thenReturn(10);
        when(properties.minSize()).thenReturn(2);
        when(trackQueueRepository.findReadyTracks(10)).thenReturn(List.of(item));
        doThrow(new RuntimeException("Liquidsoap error")).when(playbackEngine).enqueue("/music/1.mp3");

        service.enqueueTracks();

        verify(trackQueueRepository).markFailed(eq(queueId), contains("Liquidsoap error"));
        verify(trackQueueRepository, never()).markQueued(any());
    }

    @Test
    void enqueueTracks_oneTrackFailed_shouldProcessOthers() {
        UUID firstQueueId = UUID.randomUUID();
        UUID secondQueueId = UUID.randomUUID();
        TrackQueueItem first = new TrackQueueItem(firstQueueId, UUID.randomUUID(), PlaybackStatus.READY, "/music/1.mp3", Instant.now(), null);
        TrackQueueItem second = new TrackQueueItem(secondQueueId, UUID.randomUUID(), PlaybackStatus.READY, "/music/2.mp3", Instant.now(), null);

        when(playbackEngine.getQueueLength()).thenReturn(8);
        when(properties.targetSize()).thenReturn(10);
        when(properties.minSize()).thenReturn(2);
        when(trackQueueRepository.findReadyTracks(2)).thenReturn(List.of(first, second));
        doThrow(new RuntimeException("Liquidsoap unavailable")).when(playbackEngine).enqueue("/music/1.mp3");

        service.enqueueTracks();

        InOrder inOrder = inOrder(playbackEngine, trackQueueRepository);

        inOrder.verify(playbackEngine).enqueue("/music/1.mp3");
        inOrder.verify(trackQueueRepository).markFailed(eq(firstQueueId), anyString());
        inOrder.verify(playbackEngine).enqueue("/music/2.mp3");
        inOrder.verify(trackQueueRepository).markQueued(secondQueueId);
    }
}
