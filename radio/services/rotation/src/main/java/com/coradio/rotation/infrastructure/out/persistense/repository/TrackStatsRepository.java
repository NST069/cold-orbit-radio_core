package com.coradio.rotation.infrastructure.out.persistense.repository;

import com.coradio.rotation.infrastructure.out.persistense.entity.TrackStatsEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.math.BigInteger;
import java.util.UUID;

public interface TrackStatsRepository extends JpaRepository<TrackStatsEntity, UUID> {

    @Modifying
    @Query(value = """
        INSERT INTO track_stats (
            track_id,
            plays,
            likes
            )
        VALUES (
            :trackId,
            :plays,
            :likes
            )
        ON CONFLICT (track_id)
        DO UPDATE SET
            plays = track_stats.plays + EXCLUDED.plays,
            likes = track_stats.likes + EXCLUDED.likes
        """, nativeQuery = true)
    void upsert(
            @Param("trackId") UUID trackId,
            @Param("plays") BigInteger plays,
            @Param("likes") BigInteger likes
    );
}
