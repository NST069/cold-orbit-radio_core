package com.coradio.rotation.infrastructure.in.scheduler;

import com.coradio.rotation.domain.port.in.SyncIcecastStatusUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SyncIcecastStatusJob {

    private final SyncIcecastStatusUseCase syncIcecastStatusUseCase;

    @Scheduled(cron = "${rotation.jobs.sync-icecast-cron}")
    public void execute() {
        try {
            syncIcecastStatusUseCase.sync();
        } catch (Exception ex) {
            log.error("SyncIcecastStatus job failed", ex);
        }
    }
}
