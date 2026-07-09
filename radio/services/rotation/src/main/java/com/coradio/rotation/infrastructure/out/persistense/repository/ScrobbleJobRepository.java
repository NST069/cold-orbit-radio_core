package com.coradio.rotation.infrastructure.out.persistense.repository;

import com.coradio.rotation.domain.enums.JobStatus;
import com.coradio.rotation.infrastructure.out.persistense.entity.ScrobbleJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScrobbleJobRepository extends JpaRepository<ScrobbleJobEntity, UUID> {
    List<ScrobbleJobEntity> findAllByStatus(JobStatus jobStatus);
}
