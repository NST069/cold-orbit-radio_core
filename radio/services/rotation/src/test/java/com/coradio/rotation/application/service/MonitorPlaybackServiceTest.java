package com.coradio.rotation.application.service;

import com.coradio.rotation.application.exception.PlaybackPreparationException;
import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.port.in.PlaybackResyncUseCase;
import com.coradio.rotation.domain.port.out.liquidsoap.PlaybackEnginePort;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitorPlaybackServiceTest {

    @Mock
    private PlaybackEnginePort playbackEngine;

    @Mock
    private TrackQueueRepositoryPort trackQueueRepository;

    @Mock
    private PlaybackResyncUseCase playbackResyncService;

    @InjectMocks
    private MonitorPlaybackService service;

    @Test
    void shouldResyncWhenLiquidsoapQueueEmptyButDatabaseHasActiveTracks() {

        when(playbackEngine.getQueueLength()).thenReturn(0);

        when(trackQueueRepository.existsByStatusIn(List.of(PlaybackStatus.PLAYING, PlaybackStatus.QUEUED))).thenReturn(true);

        service.monitorPlayback();

        verify(playbackResyncService).resync();
    }


    @Test
    void shouldNotResyncWhenLiquidsoapQueueEmptyAndDatabaseHasNoActiveTracks() {

        when(playbackEngine.getQueueLength()).thenReturn(0);

        when(trackQueueRepository.existsByStatusIn(List.of(PlaybackStatus.PLAYING, PlaybackStatus.QUEUED))).thenReturn(false);

        service.monitorPlayback();

        verify(playbackResyncService, never()).resync();
    }


    @Test
    void shouldNotFailWhenPlaybackEngineThrowsException() {

        when(playbackEngine.getQueueLength()).thenThrow(new PlaybackPreparationException("Liquidsoap unavailable"));

        service.monitorPlayback();

        verifyNoInteractions(playbackResyncService);
    }

}
