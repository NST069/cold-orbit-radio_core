package com.coradio.rotation.infrastructure.in.scheduler;

import com.coradio.rotation.domain.port.in.PrepareTracksUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PrepareTracksJob {

    private final PrepareTracksUseCase prepareTracksUseCase;

    @Scheduled(cron = "${rotation.jobs.prepare-tracks-cron}")
    public void execute() {
        try {
            prepareTracksUseCase.prepareTracks();
        } catch (Exception ex) {
            log.error("PrepareTracks job failed", ex);
        }
    }
}
