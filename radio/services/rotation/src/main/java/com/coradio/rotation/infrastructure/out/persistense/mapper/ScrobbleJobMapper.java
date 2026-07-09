package com.coradio.rotation.infrastructure.out.persistense.mapper;

import com.coradio.rotation.domain.model.ScrobbleJobItem;
import com.coradio.rotation.infrastructure.out.persistense.entity.ScrobbleJobEntity;

public class ScrobbleJobMapper {

    public static ScrobbleJobItem toDomain(ScrobbleJobEntity entity) {
        return new ScrobbleJobItem(
                entity.getId(),
                PlaybackHistoryMapper.toDomain(entity.getPlaybackHistoryEntity()),
                entity.getProvider(),
                entity.getStatus(),
                entity.getScheduledAt(),
                entity.getSentAt(),
                entity.getAttempts(),
                entity.getError()
        );
    }

    public static ScrobbleJobEntity toEntity(ScrobbleJobItem scrobbleJobItem) {
        return ScrobbleJobEntity.builder()
                .id(scrobbleJobItem.id())
                .playbackHistoryEntity(PlaybackHistoryMapper.toEntity(scrobbleJobItem.playbackHistoryItem()))
                .provider(scrobbleJobItem.provider())
                .status(scrobbleJobItem.status())
                .scheduledAt(scrobbleJobItem.scheduledAt())
                .sentAt(scrobbleJobItem.sentAt())
                .attempts(scrobbleJobItem.attempts())
                .error(scrobbleJobItem.error())
                .build();
    }
}
