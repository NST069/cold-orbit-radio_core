package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.port.in.PlaybackResyncUseCase;
import com.coradio.rotation.domain.port.out.liquidsoap.PlaybackEnginePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaybackResyncService implements PlaybackResyncUseCase {

    private final PlaybackEnginePort engine;
    private final PlaybackStateReconciler reconciler;

    @Transactional
    public void resync() {
        log.debug("Initiating playback resync");

        if (!engine.isAvailable()) {
            return;
        }

        reconciler.reconcile(engine.getCurrentTrack(), engine.getQueueLength());
    }

}
