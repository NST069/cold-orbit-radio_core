package com.coradio.rotation.application.service;

import com.coradio.rotation.application.dto.TrackInfo;
import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.domain.port.out.persistence.TrackCatalogPort;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import com.coradio.rotation.domain.port.out.storage.StorageGatewayPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PrepareTracksServiceTest {

    @Mock
    private TrackQueueRepositoryPort trackQueueRepository;

    @Mock
    private TrackCatalogPort trackCatalogPort;

    @Mock
    private StorageGatewayPort storageGateway;

    @InjectMocks
    private PrepareTracksService service;

    @Test
    void prepareTracks_noCreatedTracks_shouldDoNothing() {
        when(trackQueueRepository.findAllByStatus(PlaybackStatus.CREATED)).thenReturn(List.of());

        service.prepareTracks();

        verify(trackCatalogPort, never()).findById(any());
        verify(storageGateway, never()).downloadFile(any());
    }

    @Test
    void prepareTracks_validTrack_shouldPrepareTrack() {
        UUID trackId = UUID.randomUUID();
        UUID queueId = UUID.randomUUID();
        TrackQueueItem queueItem = new TrackQueueItem(queueId, trackId, PlaybackStatus.CREATED, null, Instant.now(), null);
        TrackInfo trackInfo = new TrackInfo(trackId, "Artist", "Title", 180, "storage-key");

        when(trackQueueRepository.findAllByStatus(PlaybackStatus.CREATED)).thenReturn(List.of(queueItem));
        when(trackCatalogPort.findById(trackId)).thenReturn(Optional.of(trackInfo));
        when(storageGateway.downloadFile("storage-key")).thenReturn("/music/test.mp3");

        service.prepareTracks();

        InOrder inOrder = inOrder(trackQueueRepository);

        inOrder.verify(trackQueueRepository).markDownloading(queueId);
        inOrder.verify(trackQueueRepository).markReady(queueId, "/music/test.mp3");
        verify(trackQueueRepository, never()).markFailed(any(), any());
    }

    @Test
    void prepareTrack_trackNotFound_shouldMarkFailed() {
        UUID trackId = UUID.randomUUID();
        UUID queueId = UUID.randomUUID();
        TrackQueueItem queueItem = new TrackQueueItem(queueId, trackId, PlaybackStatus.CREATED, null, Instant.now(), null);

        when(trackQueueRepository.findAllByStatus(PlaybackStatus.CREATED)).thenReturn(List.of(queueItem));
        when(trackCatalogPort.findById(trackId)).thenReturn(Optional.empty());

        service.prepareTracks();

        verify(trackQueueRepository).markDownloading(queueId);
        verify(trackQueueRepository).markFailed(eq(queueId), contains(trackId.toString()));
        verify(trackQueueRepository, never()).markReady(any(), any());
    }

    @Test
    void prepareTrack_downloadError_shouldMarkFailed() {
        UUID trackId = UUID.randomUUID();
        UUID queueId = UUID.randomUUID();
        TrackQueueItem queueItem = new TrackQueueItem(queueId, trackId, PlaybackStatus.CREATED, null, Instant.now(), null);
        TrackInfo trackInfo = new TrackInfo(trackId, "Artist", "Title", 180, "storage-key");

        when(trackQueueRepository.findAllByStatus(PlaybackStatus.CREATED)).thenReturn(List.of(queueItem));
        when(trackCatalogPort.findById(trackId)).thenReturn(Optional.of(trackInfo));
        when(storageGateway.downloadFile("storage-key")).thenReturn("");

        service.prepareTracks();

        verify(trackQueueRepository).markDownloading(queueId);
        verify(trackQueueRepository).markFailed(eq(queueId), anyString());
        verify(trackQueueRepository, never()).markReady(any(), any());
    }

    @Test
    void prepareTrack_multipleTracks_shouldPrepareAllTracks() {
        UUID trackId1 = UUID.randomUUID();
        UUID trackId2 = UUID.randomUUID();
        UUID queueId1 = UUID.randomUUID();
        UUID queueId2 = UUID.randomUUID();
        TrackQueueItem first = new TrackQueueItem(queueId1, trackId1, PlaybackStatus.CREATED, null, Instant.now(), null);
        TrackQueueItem second = new TrackQueueItem(queueId2, trackId2, PlaybackStatus.CREATED, null, Instant.now(), null);
        TrackInfo track1 = new TrackInfo(trackId1, "Artist 1", "Title 1", 180, "storage-key-1");
        TrackInfo track2 = new TrackInfo(trackId2, "Artist 2", "Title 2", 200, "storage-key-2");

        when(trackQueueRepository.findAllByStatus(PlaybackStatus.CREATED)).thenReturn(List.of(first, second));
        when(trackCatalogPort.findById(trackId1)).thenReturn(Optional.of(track1));
        when(trackCatalogPort.findById(trackId2)).thenReturn(Optional.of(track2));
        when(storageGateway.downloadFile("storage-key-1")).thenReturn("/music/1.mp3");
        when(storageGateway.downloadFile("storage-key-2")).thenReturn("/music/2.mp3");

        service.prepareTracks();

        verify(trackQueueRepository, times(2)).markDownloading(any());
        verify(trackQueueRepository, times(2)).markReady(any(), any());
    }
}
