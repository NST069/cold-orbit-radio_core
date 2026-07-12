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
                .album("Album")
                .playedAt(playedAt)
                .duration(0)
                .build();

        PlaybackHistoryItem result = PlaybackHistoryMapper.toDomain(entity);

        assertEquals(id, result.id());
        assertEquals(trackId, result.trackId());
        assertEquals("Artist", result.artist());
        assertEquals("Title", result.title());
        assertEquals("Album", result.album());
        assertEquals(playedAt, result.playedAt());
        assertEquals(0, result.duration());
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
                "Album",
                playedAt,
                0
        );

        PlaybackHistoryEntity result = PlaybackHistoryMapper.toEntity(item);

        assertEquals(id, result.getId());
        assertEquals(trackId, result.getTrackId());
        assertEquals("Artist", result.getArtist());
        assertEquals("Title", result.getTitle());
        assertEquals("Album", result.getAlbum());
        assertEquals(playedAt, result.getPlayedAt());
        assertEquals(0, result.getDuration());
    }

}
