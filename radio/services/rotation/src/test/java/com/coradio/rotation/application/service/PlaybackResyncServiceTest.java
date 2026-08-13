package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.port.out.liquidsoap.PlaybackEnginePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaybackResyncServiceTest {

    @Mock
    private PlaybackEnginePort engine;

    @Mock
    private PlaybackStateReconciler reconciler;

    @InjectMocks
    private PlaybackResyncService service;

    @Test
    void shouldSkipResyncWhenEngineUnavailable() {

        when(engine.isAvailable())
                .thenReturn(false);

        service.resync();

        verifyNoInteractions(reconciler);
    }

    @Test
    void shouldResyncState() {

        when(engine.isAvailable())
                .thenReturn(true);

        when(engine.getCurrentTrack())
                .thenReturn(Optional.of("/music/c.mp3"));

        when(engine.getQueueLength())
                .thenReturn(3);

        service.resync();

        verify(reconciler).reconcile(
                Optional.of("/music/c.mp3"),
                3
        );
    }

    @Test
    void shouldResyncEmptyEngine() {

        when(engine.isAvailable())
                .thenReturn(true);

        when(engine.getCurrentTrack())
                .thenReturn(Optional.empty());

        when(engine.getQueueLength())
                .thenReturn(0);

        service.resync();

        verify(reconciler).reconcile(
                Optional.empty(),
                0
        );
    }

}
