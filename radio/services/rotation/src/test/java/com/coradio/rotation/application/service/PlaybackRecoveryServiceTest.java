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
class PlaybackRecoveryServiceTest {

    @Mock
    private PlaybackEnginePort engine;

    @Mock
    private PlaybackStateReconciler reconciler;

    @InjectMocks
    private PlaybackRecoveryService service;

    @Test
    void shouldSkipRecoveryWhenEngineUnavailable() {

        when(engine.isAvailable())
                .thenReturn(false);

        service.recover();

        verifyNoInteractions(reconciler);
    }

    @Test
    void shouldInvokeReconciler() {

        when(engine.isAvailable())
                .thenReturn(true);

        when(engine.getCurrentTrack())
                .thenReturn(Optional.of("/music/a.mp3"));

        when(engine.getQueueLength())
                .thenReturn(5);

        service.recover();

        verify(reconciler).reconcile(
                Optional.of("/music/a.mp3"),
                5
        );
    }

    @Test
    void shouldPassEmptyStateToReconciler() {

        when(engine.isAvailable())
                .thenReturn(true);

        when(engine.getCurrentTrack())
                .thenReturn(Optional.empty());

        when(engine.getQueueLength())
                .thenReturn(0);

        service.recover();

        verify(reconciler).reconcile(
                Optional.empty(),
                0
        );
    }

}
