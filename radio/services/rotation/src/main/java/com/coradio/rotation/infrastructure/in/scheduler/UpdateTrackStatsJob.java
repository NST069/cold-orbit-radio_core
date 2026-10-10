package com.coradio.rotation.infrastructure.in.scheduler;

import com.coradio.rotation.domain.port.in.TrackStatsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateTrackStatsJob {

    private final TrackStatsUseCase trackStatsService;

    @Scheduled(cron = "${rotation.jobs.update-track-stats-cron}")
    public void flushTrackStats() {
        trackStatsService.flush();
    }
}
