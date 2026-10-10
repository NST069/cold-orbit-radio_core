package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.context.RecentTrack;
import com.coradio.rotation.domain.context.RecentTracksStateContext;
import com.coradio.rotation.domain.context.TrackStatsDelta;
import com.coradio.rotation.domain.context.TrackStatsStateContext;
import com.coradio.rotation.domain.port.out.redis.RedisCachePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RotationStateServiceTest {

    private static final String STATS_KEY = "rotation:state:stats";
    private static final String HISTORY_KEY = "rotation:state:history";

    @Mock
    private TrackStatsStateContext trackStats;

    @Mock
    private RecentTracksStateContext recentTracks;

    @Mock
    private RedisCachePort redisCache;

    @InjectMocks
    private RotationStateService service;

    @Test
    void shouldSaveStats() {
        UUID trackId = UUID.randomUUID();

        TrackStatsDelta delta = new TrackStatsDelta(5, 10);

        Map<UUID, TrackStatsDelta> stats = Map.of(
                trackId, delta
        );

        when(trackStats.getStats()).thenReturn(stats);

        service.save();

        verify(redisCache).put(eq(STATS_KEY), anyString());

        String json = capturePutValue(STATS_KEY);

        assertThat(json).contains(trackId.toString());

        verify(trackStats).getStats();
    }

    @Test
    void shouldSaveHistory() {
        UUID trackId = UUID.randomUUID();

        RecentTrack track = new RecentTrack(
                trackId,
                "Artist",
                "Title",
                123456789L
        );

        Deque<RecentTrack> history = new ArrayDeque<>();
        history.add(track);

        when(recentTracks.getRecent()).thenReturn(history);

        service.save();

        verify(redisCache).put(eq(HISTORY_KEY), anyString());

        String json = capturePutValue(HISTORY_KEY);

        assertThat(json).contains(trackId.toString()).contains("Artist").contains("Title");

        verify(recentTracks).getRecent();
    }

    @Test
    void shouldSaveStatsAndHistory() {
        when(trackStats.getStats()).thenReturn(Map.of());
        when(recentTracks.getRecent()).thenReturn(new ArrayDeque<>());

        service.save();

        verify(redisCache).put(eq(STATS_KEY), anyString());
        verify(redisCache).put(eq(HISTORY_KEY), anyString());
    }

    @Test
    void shouldSaveEmptyStats() {
        when(trackStats.getStats()).thenReturn(Map.of());

        service.save();

        verify(redisCache).put(eq(STATS_KEY), eq("{}"));
    }

    @Test
    void shouldSaveEmptyHistory() {
        when(recentTracks.getRecent()).thenReturn(new ArrayDeque<>());

        service.save();

        verify(redisCache).put(eq(HISTORY_KEY), eq("[]"));
    }

    @Test
    void shouldRestoreStats() {
        UUID trackId = UUID.randomUUID();

        TrackStatsDelta delta = new TrackStatsDelta(5, 10);

        String json = """
                {
                  "%s": {
                    "plays": 5,
                    "likes": 10
                  }
                }
                """.formatted(trackId);

        when(redisCache.get(STATS_KEY)).thenReturn(Optional.of(json));

        service.restore();

        ArgumentCaptor<Map<UUID, TrackStatsDelta>> captor = ArgumentCaptor.forClass(Map.class);

        verify(trackStats).replace(captor.capture());

        Map<UUID, TrackStatsDelta> restored = captor.getValue();

        assertThat(restored).containsKey(trackId);
        assertThat(restored.get(trackId).getPlays()).isEqualTo(delta.getPlays());
        assertThat(restored.get(trackId).getLikes()).isEqualTo(delta.getLikes());
    }

    @Test
    void shouldRestoreHistory() {
        UUID trackId = UUID.randomUUID();

        String json = """
                [
                  {
                    "trackId": "%s",
                    "artist": "Artist",
                    "title": "Title",
                    "playedAt": 123456789
                  }
                ]
                """.formatted(trackId);

        when(redisCache.get(STATS_KEY)).thenReturn(Optional.empty());
        when(redisCache.get(HISTORY_KEY)).thenReturn(Optional.of(json));

        service.restore();

        ArgumentCaptor<Deque<RecentTrack>> captor = ArgumentCaptor.forClass(Deque.class);

        verify(recentTracks).replace(captor.capture());

        Deque<RecentTrack> restored = captor.getValue();

        assertThat(restored).containsExactly(new RecentTrack(trackId, "Artist", "Title", 123456789L));
    }

    @Test
    void shouldRestoreStatsAndHistory() {
        UUID trackId = UUID.randomUUID();

        String statsJson = """
                {
                  "%s": {
                    "playCount": 5,
                    "totalDuration": 10
                  }
                }
                """.formatted(trackId);

        String historyJson = """
                [
                  {
                    "trackId": "%s",
                    "artist": "Artist",
                    "title": "Title",
                    "playedAt": 123456789
                  }
                ]
                """.formatted(trackId);

        when(redisCache.get(STATS_KEY)).thenReturn(Optional.of(statsJson));
        when(redisCache.get(HISTORY_KEY)).thenReturn(Optional.of(historyJson));

        service.restore();

        verify(recentTracks).replace(any());
    }

    @Test
    void shouldDoNothingWhenStatsAreAbsent() {
        when(redisCache.get(STATS_KEY)).thenReturn(Optional.empty());
        when(redisCache.get(HISTORY_KEY)).thenReturn(Optional.empty());

        service.restore();

        verify(trackStats, never()).replace(any());
        verify(recentTracks, never()).replace(any());
    }

    @Test
    void shouldRestoreHistoryWhenStatsAreAbsent() {
        UUID trackId = UUID.randomUUID();

        String historyJson = """
                [
                  {
                    "trackId": "%s",
                    "artist": "Artist",
                    "title": "Title",
                    "playedAt": 123456789
                  }
                ]
                """.formatted(trackId);

        when(redisCache.get(STATS_KEY)).thenReturn(Optional.empty());
        when(redisCache.get(HISTORY_KEY)).thenReturn(Optional.of(historyJson));

        service.restore();

        verify(trackStats, never()).replace(any());
        verify(recentTracks).replace(any());
    }

    @Test
    void shouldRestoreStatsWhenHistoryIsAbsent() {
        UUID trackId = UUID.randomUUID();

        String statsJson = """
                {
                  "%s": {
                    "playCount": 5,
                    "totalDuration": 10
                  }
                }
                """.formatted(trackId);

        when(redisCache.get(STATS_KEY)).thenReturn(Optional.of(statsJson));
        when(redisCache.get(HISTORY_KEY)).thenReturn(Optional.empty());

        service.restore();

        verify(recentTracks, never()).replace(any());
    }

    @Test
    void shouldNotReplaceStatsWhenStatsJsonIsInvalid() {
        when(redisCache.get(STATS_KEY)).thenReturn(Optional.of("invalid json"));
        when(redisCache.get(HISTORY_KEY)).thenReturn(Optional.empty());

        service.restore();

        verify(trackStats, never()).replace(any());
    }

    @Test
    void shouldNotReplaceHistoryWhenHistoryJsonIsInvalid() {
        when(redisCache.get(STATS_KEY)).thenReturn(Optional.empty());
        when(redisCache.get(HISTORY_KEY)).thenReturn(Optional.of("invalid json"));

        service.restore();

        verify(recentTracks, never()).replace(any());
    }

    @Test
    void shouldContinueRestoringHistoryWhenStatsJsonIsInvalid() {
        UUID trackId = UUID.randomUUID();

        String historyJson = """
                [
                  {
                    "trackId": "%s",
                    "artist": "Artist",
                    "title": "Title",
                    "playedAt": 123456789
                  }
                ]
                """.formatted(trackId);

        when(redisCache.get(STATS_KEY)).thenReturn(Optional.of("invalid json"));
        when(redisCache.get(HISTORY_KEY)).thenReturn(Optional.of(historyJson));

        service.restore();

        verify(trackStats, never()).replace(any());
        verify(recentTracks).replace(any());
    }

    @Test
    void shouldContinueRestoringStatsWhenHistoryJsonIsInvalid() {
        UUID trackId = UUID.randomUUID();

        String statsJson = """
                {
                  "%s": {
                    "playCount": 5,
                    "totalDuration": 10
                  }
                }
                """.formatted(trackId);

        when(redisCache.get(STATS_KEY)).thenReturn(Optional.of(statsJson));
        when(redisCache.get(HISTORY_KEY)).thenReturn(Optional.of("invalid json"));

        service.restore();

        verify(recentTracks, never()).replace(any());
    }

    @Test
    void shouldNotWriteStatsWhenStatsSerializationFails() {
        when(trackStats.getStats()).thenReturn(Map.of());

        service.save();

        verify(redisCache).put(eq(STATS_KEY), eq("{}"));
    }

    @Test
    void shouldNotWriteHistoryWhenHistorySerializationFails() {
        when(recentTracks.getRecent()).thenReturn(new ArrayDeque<>());

        service.save();

        verify(redisCache).put(eq(HISTORY_KEY), eq("[]"));
    }

    private String capturePutValue(String key) {
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

        verify(redisCache).put(eq(key), valueCaptor.capture());

        return valueCaptor.getValue();
    }
}
