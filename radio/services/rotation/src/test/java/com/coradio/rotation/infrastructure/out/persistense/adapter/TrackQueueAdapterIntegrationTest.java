package com.coradio.rotation.infrastructure.out.persistense.adapter;

import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.infrastructure.out.persistense.entity.TrackQueueEntity;
import com.coradio.rotation.infrastructure.out.persistense.repository.TrackQueueRepository;
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
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class TrackQueueAdapterIntegrationTest {

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
    private TrackQueueRepository trackQueueRepository;

    @Autowired
    private TrackQueueAdapter adapter;

    @BeforeEach
    void setUp() {
        trackQueueRepository.deleteAll();
    }

    @Test
    void shouldFindReadyTracksOrderedByCreatedAt() {
        Instant now = Instant.now();

        TrackQueueEntity oldest = trackQueueRepository.save(
                TrackQueueEntity.builder()
                        .trackId(UUID.randomUUID())
                        .status(PlaybackStatus.READY)
                        .localPath("/music/1.mp3")
                        .createdAt(now.minusSeconds(300))
                        .build()
        );

        TrackQueueEntity middle = trackQueueRepository.save(
                TrackQueueEntity.builder()
                        .trackId(UUID.randomUUID())
                        .status(PlaybackStatus.READY)
                        .localPath("/music/2.mp3")
                        .createdAt(now.minusSeconds(200))
                        .build()
        );

        TrackQueueEntity newest = trackQueueRepository.save(
                TrackQueueEntity.builder()
                        .trackId(UUID.randomUUID())
                        .status(PlaybackStatus.READY)
                        .localPath("/music/3.mp3")
                        .createdAt(now.minusSeconds(100))
                        .build()
        );

        List<TrackQueueItem> result = adapter.findReadyTracks(2);

        assertEquals(2, result.size());
        assertEquals(oldest.getTrackId(), result.get(0).trackId());
        assertEquals(middle.getTrackId(), result.get(1).trackId());
    }

    @Test
    void shouldFindActiveTrackIds() {
        UUID readyTrack = UUID.randomUUID();
        UUID queuedTrack = UUID.randomUUID();
        UUID playingTrack = UUID.randomUUID();
        UUID failedTrack = UUID.randomUUID();
        UUID playedTrack = UUID.randomUUID();

        trackQueueRepository.save(
                TrackQueueEntity.builder()
                        .trackId(readyTrack)
                        .status(PlaybackStatus.READY)
                        .createdAt(Instant.now())
                        .build()
        );

        trackQueueRepository.save(
                TrackQueueEntity.builder()
                        .trackId(queuedTrack)
                        .status(PlaybackStatus.QUEUED)
                        .createdAt(Instant.now())
                        .build()
        );

        trackQueueRepository.save(
                TrackQueueEntity.builder()
                        .trackId(playingTrack)
                        .status(PlaybackStatus.PLAYING)
                        .createdAt(Instant.now())
                        .build()
        );

        trackQueueRepository.save(
                TrackQueueEntity.builder()
                        .trackId(failedTrack)
                        .status(PlaybackStatus.FAILED)
                        .createdAt(Instant.now())
                        .build()
        );

        trackQueueRepository.save(
                TrackQueueEntity.builder()
                        .trackId(playedTrack)
                        .status(PlaybackStatus.PLAYED)
                        .createdAt(Instant.now())
                        .build()
        );

        List<UUID> result = adapter.findActiveTrackIds();

        assertEquals(3, result.size());
        assertTrue(result.contains(readyTrack));
        assertTrue(result.contains(queuedTrack));
        assertTrue(result.contains(playingTrack));
        assertFalse(result.contains(failedTrack));
        assertFalse(result.contains(playedTrack));
    }

    @Test
    void shouldMarkDownloading() {

        UUID id = createTrack(PlaybackStatus.CREATED);

        adapter.markDownloading(id);

        TrackQueueEntity entity = trackQueueRepository.findById(id).orElseThrow();

        assertEquals(PlaybackStatus.DOWNLOADING, entity.getStatus());
    }

    @Test
    void shouldMarkReady() {
        UUID id = createTrack(PlaybackStatus.DOWNLOADING);

        adapter.markReady(id, "/music/test.mp3");

        TrackQueueEntity entity = trackQueueRepository.findById(id).orElseThrow();

        assertEquals(PlaybackStatus.READY, entity.getStatus());
        assertEquals("/music/test.mp3", entity.getLocalPath());
    }

    @Test
    void shouldMarkFailed() {
        UUID id = createTrack(PlaybackStatus.DOWNLOADING);

        adapter.markFailed(id, "failed");

        TrackQueueEntity entity = trackQueueRepository.findById(id).orElseThrow();

        assertEquals(PlaybackStatus.FAILED, entity.getStatus());
        assertEquals("failed", entity.getLastError());
    }

    @Test
    void shouldMarkQueued() {

        UUID id = createTrack(PlaybackStatus.READY);

        adapter.markQueued(id);

        TrackQueueEntity entity = trackQueueRepository.findById(id).orElseThrow();

        assertEquals(PlaybackStatus.QUEUED, entity.getStatus());
    }

    @Test
    void shouldMarkPlaying() {

        UUID id = createTrack(PlaybackStatus.QUEUED);

        adapter.markPlaying(id);

        TrackQueueEntity entity = trackQueueRepository.findById(id).orElseThrow();

        assertEquals(PlaybackStatus.PLAYING, entity.getStatus());
    }

    @Test
    void shouldMarkPlayed() {

        UUID id = createTrack(PlaybackStatus.PLAYING);

        adapter.markPlayed(id);

        TrackQueueEntity entity = trackQueueRepository.findById(id).orElseThrow();

        assertEquals(PlaybackStatus.PLAYED, entity.getStatus());
    }

    private UUID createTrack(PlaybackStatus status) {
        TrackQueueEntity entity =
                trackQueueRepository.save(
                        TrackQueueEntity.builder()
                                .trackId(UUID.randomUUID())
                                .status(status)
                                .createdAt(Instant.now())
                                .build()
                );

        return entity.getId();
    }
}
