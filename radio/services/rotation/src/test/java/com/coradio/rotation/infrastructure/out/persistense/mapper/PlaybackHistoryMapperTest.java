package com.coradio.rotation.infrastructure.out.persistense.mapper;

import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.infrastructure.out.persistense.entity.PlaybackHistoryEntity;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaybackHistoryMapperTest {

    @Test
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        UUID trackId = UUID.randomUUID();
        Instant playedAt = Instant.now();

        PlaybackHistoryEntity entity = PlaybackHistoryEntity.builder()
                .id(id)
                .trackId(trackId)
                .artist("Artist")
                .title("Title")
                .playedAt(playedAt)
                .build();

        PlaybackHistoryItem result = PlaybackHistoryMapper.toDomain(entity);

        assertEquals(id, result.id());
        assertEquals(trackId, result.trackId());
        assertEquals("Artist", result.artist());
        assertEquals("Title", result.title());
        assertEquals(playedAt, result.playedAt());
    }

    @Test
    void shouldMapDomainToEntity() {
        UUID id = UUID.randomUUID();
        UUID trackId = UUID.randomUUID();
        Instant playedAt = Instant.now();

        PlaybackHistoryItem item = new PlaybackHistoryItem(
                id,
                trackId,
                "Artist",
                "Title",
                playedAt
        );

        PlaybackHistoryEntity result = PlaybackHistoryMapper.toEntity(item);

        assertEquals(id, result.getId());
        assertEquals(trackId, result.getTrackId());
        assertEquals("Artist", result.getArtist());
        assertEquals("Title", result.getTitle());
        assertEquals(playedAt, result.getPlayedAt());
    }
}
