package com.coradio.rotation.infrastructure.out.persistense.repository;

import com.coradio.rotation.infrastructure.out.persistense.entity.PlaybackHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlaybackHistoryRepository extends JpaRepository<PlaybackHistoryEntity, UUID> {

    List<PlaybackHistoryEntity> findAllByPlayedAtAfter(Instant threshold);

    Optional<PlaybackHistoryEntity> findTopByArtistAndTitleOrderByPlayedAtDesc(String artist, String title);

}
