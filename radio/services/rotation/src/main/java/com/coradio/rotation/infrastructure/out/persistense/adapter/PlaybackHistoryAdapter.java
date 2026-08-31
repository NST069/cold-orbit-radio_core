package com.coradio.rotation.infrastructure.out.persistense.adapter;

import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.port.out.persistence.PlaybackHistoryRepositoryPort;
import com.coradio.rotation.infrastructure.out.persistense.mapper.PlaybackHistoryMapper;
import com.coradio.rotation.infrastructure.out.persistense.repository.PlaybackHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PlaybackHistoryAdapter implements PlaybackHistoryRepositoryPort {

    private final PlaybackHistoryRepository playbackHistoryRepository;

    @Override
    public PlaybackHistoryItem save(PlaybackHistoryItem playbackHistoryItem) {
        return PlaybackHistoryMapper.toDomain(
                playbackHistoryRepository.save(
                        PlaybackHistoryMapper.toEntity(playbackHistoryItem)
                )
        );
    }

    @Override
    public List<PlaybackHistoryItem> findAllInRange(long hours) {
        return playbackHistoryRepository.findAllByPlayedAtAfter(Instant.now().minus(hours, ChronoUnit.HOURS)).stream()
                .map(PlaybackHistoryMapper::toDomain).toList();
    }

    @Override
    public Optional<PlaybackHistoryItem> findLatestByArtistAndTitle(String artist, String title) {
        return playbackHistoryRepository.findTopByArtistAndTitleOrderByPlayedAtDesc(artist, title)
                .map(PlaybackHistoryMapper::toDomain);
    }

    @Override
    public List<PlaybackHistoryItem> findLast10PlayedTracks() {
        return playbackHistoryRepository.findTop10ByOrderByPlayedAtDesc().stream()
                .map(PlaybackHistoryMapper::toDomain).toList();
    }

}
