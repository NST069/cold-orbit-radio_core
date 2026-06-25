package com.coradio.rotation.infrastructure.out.persistense.adapter;

import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.infrastructure.out.persistense.entity.PlaybackHistoryEntity;
import com.coradio.rotation.infrastructure.out.persistense.repository.PlaybackHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class PlaybackHistoryAdapterIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private PlaybackHistoryAdapter adapter;

    @Autowired
    private PlaybackHistoryRepository playbackHistoryRepository;

    @BeforeEach
    void cleanup() {
        playbackHistoryRepository.deleteAll();
    }

    @Test
    void shouldSavePlaybackHistoryItem() {
        UUID trackId = UUID.randomUUID();
        Instant playedAt = Instant.now();

        PlaybackHistoryItem item = new PlaybackHistoryItem(null, trackId, "Artist", "Title", playedAt);

        PlaybackHistoryItem saved = adapter.save(item);

        assertNotNull(saved.id());
        assertEquals(trackId, saved.trackId());
        assertEquals("Artist", saved.artist());
        assertEquals("Title", saved.title());
        assertEquals(playedAt, saved.playedAt());

        List<PlaybackHistoryEntity> entities = playbackHistoryRepository.findAll();

        assertEquals(1, entities.size());

        PlaybackHistoryEntity entity = entities.getFirst();

        assertEquals(trackId, entity.getTrackId());
        assertEquals("Artist", entity.getArtist());
        assertEquals("Title", entity.getTitle());
        assertTrue(Duration.between(playedAt, saved.playedAt()).abs().toMillis() < 1);
    }

    @Test
    void shouldFindHistoryInRange() {
        PlaybackHistoryEntity oldTrack =
                PlaybackHistoryEntity.builder()
                        .trackId(UUID.randomUUID())
                        .artist("Old")
                        .title("Old")
                        .playedAt(Instant.now().minus(5, ChronoUnit.HOURS))
                        .build();

        PlaybackHistoryEntity newTrack =
                PlaybackHistoryEntity.builder()
                        .trackId(UUID.randomUUID())
                        .artist("New")
                        .title("New")
                        .playedAt(Instant.now().minus(30, ChronoUnit.MINUTES))
                        .build();

        playbackHistoryRepository.save(oldTrack);
        playbackHistoryRepository.save(newTrack);

        List<PlaybackHistoryItem> result = adapter.findAllInRange(1);

        assertEquals(1, result.size());
        assertEquals(newTrack.getTrackId(), result.getFirst().trackId());
    }

    @Test
    void shouldReturnEmptyListWhenNoTracksInRange() {

        playbackHistoryRepository.save(
                PlaybackHistoryEntity.builder()
                        .trackId(UUID.randomUUID())
                        .artist("Old")
                        .title("Old")
                        .playedAt(
                                Instant.now().minus(10, ChronoUnit.HOURS)
                        )
                        .build()
        );

        List<PlaybackHistoryItem> result = adapter.findAllInRange(1);

        assertTrue(result.isEmpty());
    }

}
