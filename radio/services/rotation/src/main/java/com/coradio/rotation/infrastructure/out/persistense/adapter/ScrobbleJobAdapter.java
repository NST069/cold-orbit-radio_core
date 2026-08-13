package com.coradio.rotation.infrastructure.out.persistense.adapter;

import com.coradio.rotation.domain.enums.JobStatus;
import com.coradio.rotation.domain.model.ScrobbleJobItem;
import com.coradio.rotation.domain.port.out.persistence.ScrobbleJobRepositoryPort;
import com.coradio.rotation.infrastructure.out.persistense.mapper.ScrobbleJobMapper;
import com.coradio.rotation.infrastructure.out.persistense.repository.ScrobbleJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScrobbleJobAdapter implements ScrobbleJobRepositoryPort {

    private final ScrobbleJobRepository scrobbleJobRepository;

    @Override
    public ScrobbleJobItem save(ScrobbleJobItem scrobbleJobItem) {
        return ScrobbleJobMapper.toDomain(
                scrobbleJobRepository.save(
                        ScrobbleJobMapper.toEntity(scrobbleJobItem)
                )
        );
    }

    @Override
    public List<ScrobbleJobItem> getAllPending() {
        List<ScrobbleJobItem> createdJobs = scrobbleJobRepository.findAllByStatus(JobStatus.CREATED).stream()
                .map(ScrobbleJobMapper::toDomain).toList();
        List<ScrobbleJobItem> failedJobs = scrobbleJobRepository.findAllByStatus(JobStatus.FAILED).stream()
                .map(ScrobbleJobMapper::toDomain).toList();
        if (!createdJobs.isEmpty() || !failedJobs.isEmpty())
            log.debug("[Scrobble jobs] Created: {}, For retry: {}", createdJobs.size(), failedJobs.size());

        return Stream.concat(createdJobs.stream(), failedJobs.stream()).toList();
    }

    @Override
    public void markRunning(UUID jobId) {
        scrobbleJobRepository.markRunning(jobId);
    }

    @Override
    public void markDone(UUID jobId) {
        scrobbleJobRepository.markDone(jobId);
    }

    @Override
    public void markFailed(UUID jobId, String reason) {
        scrobbleJobRepository.markFailed(jobId, reason);
    }

    @Override
    public void markFailedPermanently(UUID jobId, String reason) {
        scrobbleJobRepository.markFailedPermanently(jobId, reason);
    }

    @Override
    public void resetJobs(Instant threshold) {
        scrobbleJobRepository.resetJobs(threshold);
    }

}
