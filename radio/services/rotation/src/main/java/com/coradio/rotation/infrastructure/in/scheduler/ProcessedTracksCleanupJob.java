package com.coradio.rotation.infrastructure.in.scheduler;

import com.coradio.rotation.domain.port.in.CleanupUseCase;
import com.coradio.rotation.application.config.GcProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProcessedTracksCleanupJob {

    private final CleanupUseCase cleanupService;

    private final GcProperties properties;

    @Scheduled(cron = "${rotation.jobs.gc-processed-cron}")
    public void execute() {
        cleanupService.cleanupProcessedTracks(properties.processedRetention());
    }

}