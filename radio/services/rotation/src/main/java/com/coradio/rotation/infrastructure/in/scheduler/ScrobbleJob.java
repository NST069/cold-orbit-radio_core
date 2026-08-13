package com.coradio.rotation.infrastructure.in.scheduler;

import com.coradio.rotation.domain.port.in.ProcessScrobbleJobsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScrobbleJob {

    private final ProcessScrobbleJobsUseCase processScrobbleJobsUseCase;

    @Scheduled(cron = "${rotation.jobs.scrobble-cron}")
    public void execute() {
        try {
            processScrobbleJobsUseCase.process();
        } catch (Exception ex) {
            log.error("Scrobble job failed", ex);
        }
    }

}
