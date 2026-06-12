package com.coradio.rotation.infrastructure.out.persistense.repository;

import com.coradio.rotation.infrastructure.out.persistense.entity.PlaybackHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PlaybackHistoryRepository extends JpaRepository<PlaybackHistoryEntity, UUID> {
}
