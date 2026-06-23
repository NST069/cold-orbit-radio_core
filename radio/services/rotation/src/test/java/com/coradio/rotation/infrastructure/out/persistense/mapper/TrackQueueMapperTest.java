package com.coradio.rotation.infrastructure.out.persistense.mapper;

import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.infrastructure.out.persistense.entity.TrackQueueEntity;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackQueueMapperTest {

    @Test
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        UUID trackId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        TrackQueueEntity entity = TrackQueueEntity.builder()
                .id(id)
                .trackId(trackId)
                .status(PlaybackStatus.QUEUED)
                .localPath("/music/test.mp3")
                .createdAt(createdAt)
                .playedAt(null)
                .build();

        TrackQueueItem result = TrackQueueMapper.toDomain(entity);

        assertEquals(id, result.id());
        assertEquals(trackId, result.trackId());
        assertEquals(PlaybackStatus.QUEUED, result.status());
        assertEquals("/music/test.mp3", result.localPath());
        assertEquals(createdAt, result.createdAt());
    }

    @Test
    void shouldMapDomainToEntity() {

        UUID id = UUID.randomUUID();
        UUID trackId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        TrackQueueItem item = new TrackQueueItem(
                id,
                trackId,
                PlaybackStatus.READY,
                "/music/test.mp3",
                createdAt,
                null
        );

        TrackQueueEntity result = TrackQueueMapper.toEntity(item);

        assertEquals(id, result.getId());
        assertEquals(trackId, result.getTrackId());
        assertEquals(PlaybackStatus.READY, result.getStatus());
        assertEquals("/music/test.mp3", result.getLocalPath());
        assertEquals(createdAt, result.getCreatedAt());
    }
}
