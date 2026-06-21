package com.coradio.rotation.infrastructure.out.dj;

import com.coradio.rotation.application.dto.TrackInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
class RandomTrackSelectionStrategyTest {

    @InjectMocks
    private RandomTrackSelectionStrategy strategy;

    private List<TrackInfo> candidates;

    @BeforeEach
    void setUp() {
        candidates = List.of(
                new TrackInfo(UUID.randomUUID(), "Artist 1", "Title 1", 180, "track1.mp3"),
                new TrackInfo(UUID.randomUUID(), "Artist 2", "Title 2", 180, "track2.mp3"),
                new TrackInfo(UUID.randomUUID(), "Artist 3", "Title 3", 180, "track3.mp3")
        );
    }

    @Test
    void selectTracks_countIsZero_shouldReturnSingleTrack() {
        List<TrackInfo> result = strategy.selectTracks(candidates, 0, List.of());

        assertEquals(1, result.size());

        assertTrue(candidates.contains(result.getFirst()));
    }

    @Test
    void selectTracks_shouldSelectOnlyTracksFromCandidates() {
        List<TrackInfo> result = strategy.selectTracks(candidates, 10, List.of());

        assertTrue(candidates.containsAll(result));
    }

    @Test
    void selectTracks_noCandidates_shouldReturnEmptyList() {
        List<TrackInfo> result = strategy.selectTracks(List.of(), 1, List.of());

        assertEquals(0, result.size());
    }

}
