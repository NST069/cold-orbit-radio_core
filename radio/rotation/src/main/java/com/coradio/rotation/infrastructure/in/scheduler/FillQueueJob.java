package com.coradio.rotation.infrastructure.in.scheduler;

import com.coradio.rotation.domain.port.in.FillQueueUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FillQueueJob {

    private final FillQueueUseCase fillQueueUseCase;

    @Scheduled(cron = "${rotation.jobs.fill-queue-cron}")
    public void execute() {
        try {
            fillQueueUseCase.fillQueue();
        } catch (Exception ex) {
            log.error("FillQueue job failed", ex);
        }
    }
}
