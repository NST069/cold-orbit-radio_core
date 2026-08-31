package com.coradio.rotation.application.service;

import com.coradio.rotation.application.dto.response.PlaybackHistoryItemDto;
import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.port.out.persistence.PlaybackHistoryRepositoryPort;
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
    PlaybackHistoryRepositoryPort playbackHistoryRepository;

    @InjectMocks
    LastPlayedService lastPlayedService;

    @Test
    void getLastPlayed_shouldReturnLastPlayed() {
        List<PlaybackHistoryItem> expected = List.of(
                new PlaybackHistoryItem(UUID.randomUUID(), UUID.randomUUID(), "artist1", "track1", "", Instant.now().minus(1, ChronoUnit.MINUTES), 100),
                new PlaybackHistoryItem(UUID.randomUUID(), UUID.randomUUID(), "artist2", "track2", "", Instant.now().minus(5, ChronoUnit.MINUTES), 100),
                new PlaybackHistoryItem(UUID.randomUUID(), UUID.randomUUID(), "artist3", "track3", "", Instant.now().minus(10, ChronoUnit.MINUTES), 100),
                new PlaybackHistoryItem(UUID.randomUUID(), UUID.randomUUID(), "artist4", "track4", "", Instant.now().minus(15, ChronoUnit.MINUTES), 100),
                new PlaybackHistoryItem(UUID.randomUUID(), UUID.randomUUID(), "artist5", "track5", "", Instant.now().minus(20, ChronoUnit.MINUTES), 100),
                new PlaybackHistoryItem(UUID.randomUUID(), UUID.randomUUID(), "artist6", "track6", "", Instant.now().minus(25, ChronoUnit.MINUTES), 100),
                new PlaybackHistoryItem(UUID.randomUUID(), UUID.randomUUID(), "artist7", "track7", "", Instant.now().minus(30, ChronoUnit.MINUTES), 100),
                new PlaybackHistoryItem(UUID.randomUUID(), UUID.randomUUID(), "artist8", "track8", "", Instant.now().minus(35, ChronoUnit.MINUTES), 100),
                new PlaybackHistoryItem(UUID.randomUUID(), UUID.randomUUID(), "artist9", "track9", "", Instant.now().minus(40, ChronoUnit.MINUTES), 100),
                new PlaybackHistoryItem(UUID.randomUUID(), UUID.randomUUID(), "artist10", "track10", "", Instant.now().minus(45, ChronoUnit.MINUTES), 100)
        );

        when(playbackHistoryRepository.findLast10PlayedTracks()).thenReturn(expected);

        List<PlaybackHistoryItemDto> result = lastPlayedService.getLastPlayed();

        verify(playbackHistoryRepository).findLast10PlayedTracks();
        assertEquals(expected.size(), result.size());
        assertEquals(expected.stream().map(track -> new PlaybackHistoryItemDto(track.artist(), track.title(), track.playedAt())).toList(), result);

    }
}
