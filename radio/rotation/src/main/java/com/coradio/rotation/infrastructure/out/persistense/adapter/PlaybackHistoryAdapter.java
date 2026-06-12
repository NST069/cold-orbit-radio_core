package com.coradio.rotation.infrastructure.out.persistense.adapter;

import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.port.out.persistence.PlaybackHistoryRepositoryPort;
import com.coradio.rotation.infrastructure.out.persistense.mapper.PlaybackHistoryMapper;
import com.coradio.rotation.infrastructure.out.persistense.repository.PlaybackHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}
