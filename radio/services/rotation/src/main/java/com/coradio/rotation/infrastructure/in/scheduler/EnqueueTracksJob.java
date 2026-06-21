package com.coradio.rotation.infrastructure.in.scheduler;

import com.coradio.rotation.domain.port.in.EnqueueTracksUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EnqueueTracksJob {

    private final EnqueueTracksUseCase enqueueTracksUseCase;

    @Scheduled(cron = "${rotation.jobs.enqueue-tracks-cron}")
    public void execute() {
        try {
            enqueueTracksUseCase.enqueueTracks();
        } catch (Exception ex) {
            log.error("EnqueueTracks job failed", ex);
        }
    }
}
