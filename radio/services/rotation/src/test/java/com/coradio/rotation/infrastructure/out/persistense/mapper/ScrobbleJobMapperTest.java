package com.coradio.rotation.infrastructure.out.persistense.mapper;

import com.coradio.rotation.domain.enums.JobStatus;
import com.coradio.rotation.domain.enums.ScrobblerProvider;
import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.model.ScrobbleJobItem;
import com.coradio.rotation.infrastructure.out.persistense.entity.PlaybackHistoryEntity;
import com.coradio.rotation.infrastructure.out.persistense.entity.ScrobbleJobEntity;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScrobbleJobMapperTest {

    @Test
    void shouldMapEntityToDomain() {
        UUID id = UUID.randomUUID();
        Instant scheduledAt = Instant.now().minus(5, ChronoUnit.MINUTES);
        Instant startedAt = Instant.now().minus(2, ChronoUnit.MINUTES);
        Instant sentAt = Instant.now().minus(1, ChronoUnit.MINUTES);

        PlaybackHistoryEntity historyEntity = PlaybackHistoryEntity.builder()
                .id(UUID.randomUUID())
                .trackId(UUID.randomUUID())
                .artist("Artist")
                .title("Title")
                .album("Album")
                .playedAt(Instant.now().minus(5, ChronoUnit.MINUTES))
                .duration(100)
                .build();

        ScrobbleJobEntity entity = ScrobbleJobEntity.builder()
                .id(id)
                .playbackHistoryEntity(historyEntity)
                .provider(ScrobblerProvider.LIBREFM)
                .status(JobStatus.CREATED)
                .scheduledAt(scheduledAt)
                .startedAt(startedAt)
                .sentAt(sentAt)
                .attempts(1)
                .error("timeout")
                .build();

        ScrobbleJobItem result = ScrobbleJobMapper.toDomain(entity);

        assertEquals(id, result.id());
        assertEquals(historyEntity.getId(), result.playbackHistoryItem().id());
        assertEquals(ScrobblerProvider.LIBREFM, result.provider());
        assertEquals(JobStatus.CREATED, result.status());
        assertEquals(scheduledAt, result.scheduledAt());
        assertEquals(startedAt, result.startedAt());
        assertEquals(sentAt, result.sentAt());
        assertEquals(1, result.attempts());
        assertEquals("timeout", result.error());
    }

    @Test
    void shouldMapDomainToEntity() {
        UUID id = UUID.randomUUID();
        Instant scheduledAt = Instant.now().minus(5, ChronoUnit.MINUTES);
        Instant startedAt = Instant.now().minus(2, ChronoUnit.MINUTES);
        Instant sentAt = Instant.now().minus(1, ChronoUnit.MINUTES);

        PlaybackHistoryItem historyItem = new PlaybackHistoryItem(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Artist",
                "Title",
                "Album",
                Instant.now().minus(5, ChronoUnit.MINUTES),
                100
        );

        ScrobbleJobItem jobItem = new ScrobbleJobItem(
                id,
                historyItem,
                ScrobblerProvider.LIBREFM,
                JobStatus.CREATED,
                scheduledAt,
                startedAt,
                sentAt,
                1,
                "timeout"
        );

        ScrobbleJobEntity result = ScrobbleJobMapper.toEntity(jobItem);

        assertEquals(id, result.getId());
        assertEquals(historyItem.id(), result.getPlaybackHistoryEntity().getId());
        assertEquals(ScrobblerProvider.LIBREFM, result.getProvider());
        assertEquals(JobStatus.CREATED, result.getStatus());
        assertEquals(scheduledAt, result.getScheduledAt());
        assertEquals(startedAt, result.getStartedAt());
        assertEquals(sentAt, result.getSentAt());
        assertEquals(1, result.getAttempts());
        assertEquals("timeout", result.getError());
    }

}
