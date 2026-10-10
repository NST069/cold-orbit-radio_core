package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.context.RecentTrack;
import com.coradio.rotation.domain.context.RecentTracksStateContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastPlayedServiceTest {

    @Mock
    private RecentTracksStateContext recentTracksStateContext;

    @InjectMocks
    private LastPlayedService lastPlayedService;

    @Test
    void getLastPlayed_shouldReturnLastPlayed() {
        List<RecentTrack> expected = List.of(
                new RecentTrack(UUID.randomUUID(), "artist1", "track1", Instant.now().minus(1, ChronoUnit.MINUTES).getEpochSecond()),
                new RecentTrack(UUID.randomUUID(), "artist2", "track2", Instant.now().minus(5, ChronoUnit.MINUTES).getEpochSecond()),
                new RecentTrack(UUID.randomUUID(), "artist3", "track3", Instant.now().minus(10, ChronoUnit.MINUTES).getEpochSecond()),
                new RecentTrack(UUID.randomUUID(), "artist4", "track4", Instant.now().minus(15, ChronoUnit.MINUTES).getEpochSecond()),
                new RecentTrack(UUID.randomUUID(), "artist5", "track5", Instant.now().minus(20, ChronoUnit.MINUTES).getEpochSecond()),
                new RecentTrack(UUID.randomUUID(), "artist6", "track6", Instant.now().minus(25, ChronoUnit.MINUTES).getEpochSecond()),
                new RecentTrack(UUID.randomUUID(), "artist7", "track7", Instant.now().minus(30, ChronoUnit.MINUTES).getEpochSecond()),
                new RecentTrack(UUID.randomUUID(), "artist8", "track8", Instant.now().minus(35, ChronoUnit.MINUTES).getEpochSecond()),
                new RecentTrack(UUID.randomUUID(), "artist9", "track9", Instant.now().minus(40, ChronoUnit.MINUTES).getEpochSecond()),
                new RecentTrack(UUID.randomUUID(), "artist10", "track10", Instant.now().minus(45, ChronoUnit.MINUTES).getEpochSecond())

        );

        when(recentTracksStateContext.getRecentHistory()).thenReturn(expected);

        List<RecentTrack> result = lastPlayedService.getLastPlayed();

        verify(recentTracksStateContext).getRecentHistory();
        assertEquals(expected, result);

    }
}
