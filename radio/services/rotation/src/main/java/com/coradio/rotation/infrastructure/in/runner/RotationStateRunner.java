package com.coradio.rotation.infrastructure.in.runner;

import com.coradio.rotation.domain.port.in.RotationStateUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RotationStateRunner {

    private final RotationStateUseCase rotationStateService;

    @EventListener(ApplicationReadyEvent.class)
    public void restoreState() {
        rotationStateService.restore();
    }

}
