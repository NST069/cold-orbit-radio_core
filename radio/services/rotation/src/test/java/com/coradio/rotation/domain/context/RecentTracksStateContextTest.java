package com.coradio.rotation.domain.context;

import com.coradio.rotation.application.config.QueueProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecentTracksStateContextTest {

    @Mock
    private QueueProperties properties;

    @InjectMocks
    private RecentTracksStateContext context;

    @Test
    void shouldAddTrackToRecent() {
        UUID trackId = UUID.randomUUID();
        Instant playedAt = Instant.now();

        context.add(trackId, "Artist", "Title", playedAt);

        assertThat(context.getRecentHistory())
                .containsExactly(
                        new RecentTrack(
                                trackId,
                                "Artist",
                                "Title",
                                playedAt.toEpochMilli()
                        )
                );
    }

    @Test
    void shouldAddNewestTrackFirst() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        Instant firstPlayedAt = Instant.now();
        Instant secondPlayedAt = Instant.now();

        context.add(firstId, "Artist 1", "Title 1", firstPlayedAt);
        context.add(secondId, "Artist 2", "Title 2", secondPlayedAt);

        assertThat(context.getRecentHistory()).extracting(RecentTrack::trackId).containsExactly(secondId, firstId);
    }

    @Test
    void shouldKeepOnlyTenTracksInRecentHistory() {
        when(properties.historyHours()).thenReturn(24);

        for (int i = 0; i < 15; i++) {
            context.add(UUID.randomUUID(), "Artist " + i, "Title " + i, Instant.now().minusSeconds(i));
        }

        assertThat(context.getRecentHistory()).hasSize(10);
    }

    @Test
    void shouldReturnOnlyTenTracksFromLargerDeque() {
        when(properties.historyHours()).thenReturn(24);

        for (int i = 0; i < 15; i++) {
            context.add(UUID.randomUUID(), "Artist " + i, "Title " + i, Instant.now());
        }

        assertThat(context.getRecentHistory()).hasSize(10);
    }

    @Test
    void shouldRemoveExpiredTracksWhenDequeContainsMoreThanTenTracks() {
        when(properties.historyHours()).thenReturn(1);

        Instant base = Instant.now();

        for (int i = 0; i < 10; i++) {
            context.add(UUID.randomUUID(), "Artist " + i, "Title " + i, base.minusSeconds(i));
        }

        UUID expiredTrackId = UUID.randomUUID();

        context.add(expiredTrackId, "Expired Artist", "Expired Title", base.minusSeconds(72000));

        assertThat(context.getRecent())
                .doesNotContain(
                        new RecentTrack(
                                expiredTrackId,
                                "Expired Artist",
                                "Expired Title",
                                base.minusSeconds(7200).toEpochMilli()
                        )
                );
    }

    @Test
    void shouldNotRemoveTracksWhenHistoryLimitIsNotExceeded() {
        when(properties.historyHours()).thenReturn(1);

        Instant oldPlayedAt = Instant.now();

        UUID trackId = UUID.randomUUID();

        context.add(trackId, "Artist", "Title", oldPlayedAt);

        assertThat(context.getRecentHistory()).extracting(RecentTrack::trackId).containsExactly(trackId);
    }

    @Test
    void shouldReturnRecentTrackIdsLimitedByHalfOfCandidatesSize() {
        when(properties.historyHours()).thenReturn(24);

        List<UUID> trackIds = IntStream.range(0, 10)
                .mapToObj(i -> UUID.randomUUID())
                .toList();

        Instant playedAt = Instant.now();

        for (int i = 0; i < trackIds.size(); i++) {
            context.add(trackIds.get(i), "Artist " + i, "Title " + i, playedAt.minusSeconds(i));
        }

        assertThat(context.getRecentTrackIds(8)).containsExactlyInAnyOrderElementsOf(trackIds.subList(6, 10));
    }

    @Test
    void shouldReturnEmptyTrackIdsWhenCandidatesSizeIsOne() {
        UUID trackId = UUID.randomUUID();

        context.add(trackId, "Artist", "Title", Instant.now());

        assertThat(context.getRecentTrackIds(1)).isEmpty();
    }

    @Test
    void shouldReturnAllAvailableTrackIdsWhenCandidatesSizeIsLarge() {
        when(properties.historyHours()).thenReturn(24);

        List<UUID> trackIds = List.of(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        Instant playedAt = Instant.now();

        for (int i = 0; i < trackIds.size(); i++) {
            context.add(trackIds.get(i), "Artist " + i, "Title " + i, playedAt.minusSeconds(i));
        }

        assertThat(context.getRecentTrackIds(100)).containsExactlyInAnyOrderElementsOf(trackIds);
    }

    @Test
    void shouldReturnUniqueTrackIds() {
        when(properties.historyHours()).thenReturn(24);

        UUID trackId = UUID.randomUUID();
        Instant playedAt = Instant.now();

        context.add(trackId, "Artist", "Title", playedAt);
        context.add(trackId, "Artist", "Title", playedAt.plusSeconds(10));

        assertThat(context.getRecentTrackIds(10)).containsExactly(trackId);
    }

    @Test
    void shouldReplaceCurrentTracks() {
        UUID oldTrackId = UUID.randomUUID();
        UUID restoredTrackId = UUID.randomUUID();
        Instant playedAt = Instant.now();

        context.add(oldTrackId, "Old Artist", "Old Title", playedAt);

        RecentTrack restoredTrack = new RecentTrack(restoredTrackId, "Restored Artist", "Restored Title", playedAt.getEpochSecond());

        Deque<RecentTrack> restored = new ArrayDeque<>();
        restored.add(restoredTrack);

        context.replace(restored);

        assertThat(context.getRecentHistory()).containsExactly(restoredTrack);

        assertThat(context.getRecentHistory()).extracting(RecentTrack::trackId).doesNotContain(oldTrackId);
    }

    @Test
    void shouldPreserveOrderWhenReplacingTracks() {
        RecentTrack first = new RecentTrack(UUID.randomUUID(), "Artist 1", "Title 1", 1000L);

        RecentTrack second = new RecentTrack(UUID.randomUUID(), "Artist 2", "Title 2", 2000L);

        Deque<RecentTrack> restored = new ArrayDeque<>();
        restored.add(first);
        restored.add(second);

        context.replace(restored);

        assertThat(context.getRecentHistory()).containsExactly(first, second);
    }

    @Test
    void shouldAllowEmptyReplace() {
        context.add(UUID.randomUUID(), "Artist", "Title", Instant.now());

        context.replace(new ArrayDeque<>());

        assertThat(context.getRecent()).isEmpty();

        assertThat(context.getRecentHistory()).isEmpty();
    }
}
