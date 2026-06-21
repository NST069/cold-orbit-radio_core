package com.coradio.rotation.infrastructure.in.scheduler;

import com.coradio.rotation.domain.port.in.MonitorPlaybackUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MonitorPlaybackJob {

    private final MonitorPlaybackUseCase monitorPlaybackUseCase;

    @Scheduled(cron = "${rotation.jobs.monitor-playback-cron}")
    public void execute() {
        try {
            monitorPlaybackUseCase.monitorPlayback();
        } catch (Exception ex) {
            log.error("MonitorPlayback job failed", ex);
        }
    }
}
