package com.coradio.rotation.infrastructure.out.persistense.mapper;

import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.infrastructure.out.persistense.entity.TrackQueueEntity;

public class TrackQueueMapper {

    public static TrackQueueItem toDomain(TrackQueueEntity trackQueueEntity) {
        return new TrackQueueItem(
                trackQueueEntity.getId(),
                trackQueueEntity.getTrackId(),
                trackQueueEntity.getStatus(),
                trackQueueEntity.getLocalPath(),
                trackQueueEntity.getCreatedAt()
        );
    }

    public static TrackQueueEntity toEntity(TrackQueueItem trackQueueItem) {
        return TrackQueueEntity.builder()
                .id(trackQueueItem.id())
                .trackId(trackQueueItem.trackId())
                .status(trackQueueItem.status())
                .localPath(trackQueueItem.localPath())
                .createdAt(trackQueueItem.createdAt())
                .build();
    }
}
