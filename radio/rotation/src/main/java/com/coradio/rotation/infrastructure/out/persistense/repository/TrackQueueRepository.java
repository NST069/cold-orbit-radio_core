package com.coradio.rotation.infrastructure.out.persistense.repository;

import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.infrastructure.out.persistense.entity.TrackQueueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface TrackQueueRepository extends JpaRepository<TrackQueueEntity, UUID> {

    int deleteAllByStatus(PlaybackStatus status);

    List<TrackQueueEntity> findAllByStatus(PlaybackStatus status);

}
