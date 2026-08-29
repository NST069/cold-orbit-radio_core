package com.coradio.rotation.application.service;

import com.coradio.rotation.application.dto.response.PlaybackHistoryItemDto;
import com.coradio.rotation.domain.port.in.LastPlayedUseCase;
import com.coradio.rotation.domain.port.out.persistence.PlaybackHistoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LastPlayedService implements LastPlayedUseCase {

    private final PlaybackHistoryRepositoryPort playbackHistoryRepository;

    @Override
    public List<PlaybackHistoryItemDto> getLastPlayed() {
        return playbackHistoryRepository.findLast10PlayedTracks().stream()
                .map(track -> new PlaybackHistoryItemDto(
                        track.artist(),
                        track.title(),
                        track.playedAt())
                ).toList();
    }
}
