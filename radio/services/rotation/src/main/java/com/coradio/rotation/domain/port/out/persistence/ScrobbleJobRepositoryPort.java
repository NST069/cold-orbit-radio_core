package com.coradio.rotation.domain.port.out.persistence;

import com.coradio.rotation.domain.model.ScrobbleJobItem;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ScrobbleJobRepositoryPort {
    ScrobbleJobItem save(ScrobbleJobItem scrobbleJobItem);

    List<ScrobbleJobItem> getAllPending();

    void markRunning(UUID jobId);

    void markDone(UUID jobId);

    void markFailed(UUID jobId, String reason);

    void markFailedPermanently(UUID jobId, String reason);

    void resetJobs(Instant threshold);

}
