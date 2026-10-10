package com.coradio.notification.infrastructure.in.scheduler;

import com.coradio.notification.domain.port.out.RedisStreamPendingCleanerPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PendingEventCleanupScheduler {

    private final RedisStreamPendingCleanerPort cleaner;

    @Scheduled(cron = "${notification.jobs.stream-pending-cleanup-cron}")
    public void cleanup() {
        log.debug("Running Redis pending cleanup");

        try {
            cleaner.cleanup();
        } catch (Exception e) {
            log.error("Redis pending cleanup failed", e);
        }
    }
}
