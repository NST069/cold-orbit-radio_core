package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.port.in.PlaybackRecoveryUseCase;
import com.coradio.rotation.domain.port.out.liquidsoap.PlaybackEnginePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaybackRecoveryService implements PlaybackRecoveryUseCase {

    private final PlaybackEnginePort engine;
    private final PlaybackStateReconciler reconciler;

    @Transactional
    public void recover() {
        log.debug("Initiating playback recovery");

        if (!engine.isAvailable()) {
            return;
        }

        reconciler.reconcile(engine.getCurrentTrack(), engine.getQueueLength());
    }

}
