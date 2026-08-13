package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.model.ScrobbleJobItem;
import com.coradio.rotation.domain.port.in.ProcessScrobbleJobsUseCase;
import com.coradio.rotation.domain.port.out.persistence.ScrobbleJobRepositoryPort;
import com.coradio.rotation.domain.port.out.scrobbler.ScrobbleProviderRegistryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessScrobbleJobsService implements ProcessScrobbleJobsUseCase {

    private final ScrobbleProviderRegistryPort providerRegistry;

    private final ScrobbleJobRepositoryPort repository;

    public void process() {

        Instant threshold = Instant.now().minus(Duration.ofMinutes(5));
        repository.resetJobs(threshold);

        List<ScrobbleJobItem> scrobbleJobs = repository.getAllPending();

        scrobbleJobs.forEach(job -> {
            try {
                log.debug("Scrobbling {} by {}, attempt: {}", job.id(), job.provider(), job.attempts() + 1);

                markRunning(job.id());
                providerRegistry.get(job.provider()).scrobble(job);
                markDone(job.id());

            } catch (Exception e) {
                markFailed(job.id(), job.attempts(), e.getMessage());
                log.warn("Scrobbling by {} failed: {}", job.provider(), job.id());
            }
        });
    }

    private void markRunning(UUID jobId) {
        repository.markRunning(jobId);
    }

    private void markDone(UUID jobId) {
        repository.markDone(jobId);
    }

    private void markFailed(UUID jobId, int attempts, String reason) {
        if (attempts > 5) repository.markFailedPermanently(jobId, reason);
        else repository.markFailed(jobId, reason);
    }

}
