package com.coradio.rotation.infrastructure.out.persistense.adapter;

import com.coradio.rotation.application.dto.TrackInfo;
import com.coradio.rotation.domain.port.out.persistence.TrackCatalogPort;
import com.coradio.rotation.infrastructure.out.persistense.mapper.TrackInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TrackCatalogAdapter implements TrackCatalogPort {

    private final JdbcClient jdbcClient;

    private final TrackInfoMapper mapper;

    @Override
    public Optional<TrackInfo> findById(UUID trackId) {
        return jdbcClient.sql("""
                
                        SELECT
                    t.id,
                    t.artist,
                    t.title,
                    tf.duration,
                    tf.storage_key
                FROM tracks t
                JOIN track_files tf ON tf.track_id = t.id
                WHERE t.id = :trackId
                """)
                .param("trackId", trackId)
                .query(mapper)
                .optional();
    }

    @Override
    public List<TrackInfo> findPlayableTracks() {
        return jdbcClient.sql(
                        """
                SELECT
                    t.id,
                                        
                                     t.t
                                 tf.durati
                               tf.sto
                                FROM tracks t
                JOIN track_files tf
                        ON tf.tra
                                    WHERE tf.status = 'READY'
                """)
                .query(mapper)
                .list();
    }

}
