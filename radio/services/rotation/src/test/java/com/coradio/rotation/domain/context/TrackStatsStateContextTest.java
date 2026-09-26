package com.coradio.rotation.domain.context;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TrackStatsStateContextTest {

    @InjectMocks
    private TrackStatsStateContext context;

    @Test
    void shouldIncrementPlayCountForNewTrack() {
        UUID trackId = UUID.randomUUID();

        context.incrementPlayCount(trackId);

        TrackStatsDelta stats = context.get(trackId);

        assertThat(stats).isNotNull();
        assertThat(stats.getPlays()).isEqualTo(1);
    }

    @Test
    void shouldIncrementPlayCountForExistingTrack() {
        UUID trackId = UUID.randomUUID();

        context.incrementPlayCount(trackId);
        context.incrementPlayCount(trackId);
        context.incrementPlayCount(trackId);

        assertThat(context.get(trackId).getPlays()).isEqualTo(3);
    }

    @Test
    void shouldKeepStatsForDifferentTracksSeparately() {
        UUID firstTrackId = UUID.randomUUID();
        UUID secondTrackId = UUID.randomUUID();

        context.incrementPlayCount(firstTrackId);
        context.incrementPlayCount(firstTrackId);
        context.incrementPlayCount(secondTrackId);

        assertThat(context.get(firstTrackId).getPlays()).isEqualTo(2);
        assertThat(context.get(secondTrackId).getPlays()).isEqualTo(1);
    }

    @Test
    void shouldReturnNullForUnknownTrack() {
        UUID trackId = UUID.randomUUID();

        assertThat(context.get(trackId)).isNull();
    }

    @Test
    void shouldReplaceCurrentStats() {
        UUID oldTrackId = UUID.randomUUID();
        UUID restoredTrackId = UUID.randomUUID();

        context.incrementPlayCount(oldTrackId);

        TrackStatsDelta restoredStats = new TrackStatsDelta(5, 10);

        Map<UUID, TrackStatsDelta> restored = Map.of(
                restoredTrackId, restoredStats
        );

        context.replace(restored);

        assertThat(context.get(oldTrackId)).isNull();
        assertThat(context.get(restoredTrackId)).isSameAs(restoredStats);
    }

    @Test
    void shouldReplaceWithEmptyMap() {
        UUID trackId = UUID.randomUUID();

        context.incrementPlayCount(trackId);

        context.replace(Map.of());

        assertThat(context.getStats()).isEmpty();
    }

    @Test
    void shouldPreserveAllRestoredStats() {
        UUID firstTrackId = UUID.randomUUID();
        UUID secondTrackId = UUID.randomUUID();

        TrackStatsDelta firstStats = new TrackStatsDelta(5, 10);
        TrackStatsDelta secondStats = new TrackStatsDelta(3, 20);

        Map<UUID, TrackStatsDelta> restored = Map.of(
                firstTrackId, firstStats,
                secondTrackId, secondStats
        );

        context.replace(restored);

        assertThat(context.getStats())
                .containsEntry(firstTrackId, firstStats)
                .containsEntry(secondTrackId, secondStats);
    }

    @Test
    void shouldSwapCurrentStatsAndReturnThem() {
        UUID trackId = UUID.randomUUID();

        context.incrementPlayCount(trackId);

        Map<UUID, TrackStatsDelta> swapped = context.swap();

        assertThat(swapped).hasSize(1).containsKey(trackId);
        assertThat(context.getStats()).isEmpty();
        assertThat(context.get(trackId)).isNull();
    }

    @Test
    void shouldReturnSameMapInstanceFromSwap() {
        UUID trackId = UUID.randomUUID();

        context.incrementPlayCount(trackId);

        Map<UUID, TrackStatsDelta> originalMap = context.getStats();

        Map<UUID, TrackStatsDelta> swappedMap = context.swap();

        assertThat(swappedMap).isSameAs(originalMap);
        assertThat(context.getStats()).isNotSameAs(originalMap);
    }

    @Test
    void shouldReturnEmptyMapWhenSwappingEmptyStats() {
        Map<UUID, TrackStatsDelta> swapped = context.swap();

        assertThat(swapped).isEmpty();
        assertThat(context.getStats()).isEmpty();
        assertThat(context.getStats()).isNotSameAs(swapped);
    }

    @Test
    void shouldDrainCurrentStats() {
        UUID trackId = UUID.randomUUID();

        context.incrementPlayCount(trackId);

        Map<UUID, TrackStatsDelta> drained = context.drain();

        assertThat(drained).hasSize(1).containsKey(trackId);
        assertThat(context.getStats()).isEmpty();
        assertThat(context.get(trackId)).isNull();
    }

    @Test
    void shouldReturnSameMapInstanceFromDrain() {
        UUID trackId = UUID.randomUUID();

        context.incrementPlayCount(trackId);

        Map<UUID, TrackStatsDelta> originalMap = context.getStats();

        Map<UUID, TrackStatsDelta> drainedMap = context.drain();

        assertThat(drainedMap).isSameAs(originalMap);
        assertThat(context.getStats()).isNotSameAs(originalMap);
    }

    @Test
    void shouldDrainEmptyStats() {
        Map<UUID, TrackStatsDelta> drained = context.drain();

        assertThat(drained).isEmpty();
        assertThat(context.getStats()).isEmpty();
        assertThat(context.getStats()).isNotSameAs(drained);
    }

    @Test
    void shouldClearAllStats() {
        UUID firstTrackId = UUID.randomUUID();
        UUID secondTrackId = UUID.randomUUID();

        context.incrementPlayCount(firstTrackId);
        context.incrementPlayCount(secondTrackId);

        context.clear();

        assertThat(context.getStats()).isEmpty();
    }

    @Test
    void shouldAllowIncrementAfterClear() {
        UUID firstTrackId = UUID.randomUUID();
        UUID secondTrackId = UUID.randomUUID();

        context.incrementPlayCount(firstTrackId);
        context.clear();

        context.incrementPlayCount(secondTrackId);

        assertThat(context.get(firstTrackId)).isNull();

        assertThat(context.get(secondTrackId).getPlays()).isEqualTo(1);
    }

    @Test
    void shouldAllowIncrementAfterSwap() {
        UUID firstTrackId = UUID.randomUUID();
        UUID secondTrackId = UUID.randomUUID();

        context.incrementPlayCount(firstTrackId);

        Map<UUID, TrackStatsDelta> swapped = context.swap();

        context.incrementPlayCount(secondTrackId);

        assertThat(swapped).containsKey(firstTrackId).doesNotContainKey(secondTrackId);
        assertThat(context.getStats()).containsKey(secondTrackId).doesNotContainKey(firstTrackId);
    }

    @Test
    void shouldAllowIncrementAfterDrain() {
        UUID firstTrackId = UUID.randomUUID();
        UUID secondTrackId = UUID.randomUUID();

        context.incrementPlayCount(firstTrackId);

        Map<UUID, TrackStatsDelta> drained = context.drain();

        context.incrementPlayCount(secondTrackId);

        assertThat(drained).containsKey(firstTrackId).doesNotContainKey(secondTrackId);
        assertThat(context.getStats()).containsKey(secondTrackId).doesNotContainKey(firstTrackId);
    }
}
