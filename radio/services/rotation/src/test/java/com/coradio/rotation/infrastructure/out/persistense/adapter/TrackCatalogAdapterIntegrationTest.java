package com.coradio.rotation.infrastructure.out.persistense.adapter;

import com.coradio.rotation.application.dto.TrackInfo;
import com.coradio.rotation.infrastructure.out.persistense.mapper.TrackInfoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@Testcontainers
@JdbcTest(properties = {"spring.liquibase.contexts=test"})
@Import({
        TrackCatalogAdapter.class,
        TrackInfoMapper.class
})
class TrackCatalogAdapterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private TrackCatalogAdapter adapter;

    @BeforeEach
    void cleanup() {
        jdbcClient.sql("DELETE FROM track_files").update();
        jdbcClient.sql("DELETE FROM tracks").update();
    }

    @Test
    void shouldFindTrackById() {
        UUID trackId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        jdbcClient.sql("""
                INSERT INTO tracks(
                    id,
                    artist,
                    title,
                    duration
                )
                VALUES(
                    :id,
                    :artist,
                    :title,
                    :duration
                )
                """)
                .param("id", trackId)
                .param("artist", "Artist")
                .param("title", "Title")
                .param("duration", 180)
                .update();

        jdbcClient.sql("""
                INSERT INTO track_files(
                    id,
                    track_id,
                    storage_key,
                    status
                )
                VALUES(
                    :id,
                    :trackId,
                    :storageKey,
                    :status
                )
                """)
                .param("id", fileId)
                .param("trackId", trackId)
                .param("storageKey", "music/test.mp3")
                .param("status", "READY")
                .update();

        Optional<TrackInfo> result = adapter.findById(trackId);

        assertTrue(result.isPresent());

        TrackInfo track = result.get();

        assertEquals(trackId, track.id());
        assertEquals("Artist", track.artist());
        assertEquals("Title", track.title());
        assertEquals(180, track.duration());
        assertEquals("music/test.mp3", track.storageKey());
    }

    @Test
    void shouldReturnEmptyWhenTrackNotExists() {
        Optional<TrackInfo> result = adapter.findById(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindOnlyReadyTracks() {
        UUID readyTrack1 = insertTrack("Artist1" , "Track1",180, "READY");
        UUID readyTrack2 = insertTrack("Artist2", "Track2",200, "READY");
        insertTrack("Artist3","Track3",220,"FAILED");

        List<TrackInfo> result = adapter.findPlayableTracks();

        assertEquals(2, result.size());

        Set<UUID> ids = result.stream()  .map(TrackInfo::id) .collect(Collectors.toSet());

        assertTrue(ids.contains(readyTrack1));
        assertTrue(ids.contains(readyTrack2));
    }

    @Test
    void shouldReturnEmptyWhenNoReadyTracks() {
        insertTrack("Artis",  "Track1", 180,"FAILED");
        insertTrack( "Artist2","Track2", 200,"DOWNLOADING");

        List<TrackInfo> result =   adapter.findPlayableTracks();

        assertTrue(result.isEmpty());
    }

    private UUID insertTrack(
            String artist,
            String title,
            int duration,
            String fileStatus
    ) {
        UUID trackId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        jdbcClient.sql("""
                INSERT INTO tracks(
                    id,
                    artist,
                    title,
                    duration
                )
                VALUES(
                    :id,
                    :artist,
                    :title,
                    :duration
                )
                """)
                .param("id", trackId)
                .param("artist", artist)
                .param("title", title)
                .param("duration", duration)
                .update();

        jdbcClient.sql("""
                INSERT INTO track_files(
                    id,
                    track_id,
                    storage_key,
                    status
                )
                VALUES(
                    :id,
                    :trackId,
                    :storageKey,
                    :status
                )
                """)
                .param("id", fileId)
                .param("trackId", trackId)
                .param("storageKey", "music/" + trackId + ".mp3")
                .param("status", fileStatus)
                .update();

        return trackId;
    }
}
