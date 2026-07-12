package com.coradio.rotation.infrastructure.in.runner;

import com.coradio.rotation.domain.port.in.PlaybackRecoveryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlaybackRecoveryRunner {

    private final PlaybackRecoveryUseCase recoveryService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        recoveryService.recover();
    }

}
