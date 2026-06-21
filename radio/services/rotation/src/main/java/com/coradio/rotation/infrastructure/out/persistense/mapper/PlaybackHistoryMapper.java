package com.coradio.rotation.infrastructure.out.persistense.mapper;

import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.infrastructure.out.persistense.entity.PlaybackHistoryEntity;

public class PlaybackHistoryMapper {

    public static PlaybackHistoryItem toDomain(PlaybackHistoryEntity playbackHistoryEntity) {
        return new PlaybackHistoryItem(
                playbackHistoryEntity.getId(),
                playbackHistoryEntity.getTrackId(),
                playbackHistoryEntity.getArtist(),
                playbackHistoryEntity.getTitle(),
                playbackHistoryEntity.getPlayedAt()
        );
    }

    public static PlaybackHistoryEntity toEntity(PlaybackHistoryItem playbackHistoryItem) {
        return PlaybackHistoryEntity.builder()
                .id(playbackHistoryItem.id())
                .trackId(playbackHistoryItem.trackId())
                .artist(playbackHistoryItem.artist())
                .title(playbackHistoryItem.title())
                .playedAt(playbackHistoryItem.playedAt())
                .build();
    }
}
