package com.coradio.rotation.infrastructure.in.scheduler;

import com.coradio.rotation.domain.port.in.RotationStateUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RotationStateCachingJob {

    private final RotationStateUseCase rotationStateService;

    @Scheduled(cron = "${rotation.jobs.state-caching-cron}")
    public void saveState() {
        rotationStateService.save();
    }

}
