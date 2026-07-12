package com.coradio.rotation.infrastructure.out.persistense.repository;

import com.coradio.rotation.domain.enums.JobStatus;
import com.coradio.rotation.infrastructure.out.persistense.entity.ScrobbleJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScrobbleJobRepository extends JpaRepository<ScrobbleJobEntity, UUID> {
    List<ScrobbleJobEntity> findAllByStatus(JobStatus jobStatus);

    @Modifying
    @Transactional
    @Query(
            """
            update ScrobbleJobEntity j
            set j.status = com.coradio.rotation.domain.enums.JobStatus.RUNNING,
                j.startedAt = current_timestamp
            where j.id = :id
        """
    )
    void markRunning(UUID id);

    @Modifying
    @Transactional
    @Query(
            """
            update ScrobbleJobEntity j
            set j.status = com.coradio.rotation.domain.enums.JobStatus.DONE,
                j.sentAt = current_timestamp
            where j.id = :id
        """
    )
    void markDone(UUID id);

    @Modifying
    @Transactional
    @Query(
            """
            update ScrobbleJobEntity j
            set j.status = com.coradio.rotation.domain.enums.JobStatus.FAILED,
                j.error = :reason,
                j.attempts = j.attempts + 1
            where j.id = :id
        """
    )
    void markFailed(UUID id, String reason);

    @Modifying
    @Transactional
    @Query(
            """
            update ScrobbleJobEntity j
            set j.status = com.coradio.rotation.domain.enums.JobStatus.FAILED_PERMANENTLY,
                j.error = :reason
            where j.id = :id
        """
    )
    void markFailedPermanently(UUID id, String reason);

    @Modifying
    @Transactional
    @Query(
            """
            update ScrobbleJobEntity j
            set j.status = com.coradio.rotation.domain.enums.JobStatus.CREATED
            where j.status = com.coradio.rotation.domain.enums.JobStatus.RUNNING
              and j.startedAt < :threshold
        """
    )
    void resetJobs(Instant threshold);

}
