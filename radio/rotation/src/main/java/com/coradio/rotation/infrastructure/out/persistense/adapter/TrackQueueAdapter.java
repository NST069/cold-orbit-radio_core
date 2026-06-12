package com.coradio.rotation.infrastructure.out.persistense.adapter;

import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import com.coradio.rotation.infrastructure.out.persistense.mapper.TrackQueueMapper;
import com.coradio.rotation.infrastructure.out.persistense.repository.TrackQueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TrackQueueAdapter implements TrackQueueRepositoryPort {

    private final TrackQueueRepository trackQueueRepository;

    @Override
    public TrackQueueItem save(TrackQueueItem trackQueueItem) {
        return TrackQueueMapper.toDomain(
                trackQueueRepository.save(
                        TrackQueueMapper.toEntity(trackQueueItem)
                )
        );
    }

    @Override
    public Optional<TrackQueueItem> findById(UUID id) {
        return trackQueueRepository.findById(id)
                .map(TrackQueueMapper::toDomain);
    }

    @Override
    public List<TrackQueueItem> findAllByStatus(PlaybackStatus status) {
        return trackQueueRepository.findAllByStatus(status).stream()
                .map(TrackQueueMapper::toDomain).toList();
    }

    @Override
    public List<TrackQueueItem> findAll() {
        return trackQueueRepository.findAll().stream()
                .map(TrackQueueMapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        trackQueueRepository.deleteById(id);
    }

    @Override
    public int deleteAllByStatus(PlaybackStatus status) {
        return trackQueueRepository.deleteAllByStatus(status);
    }
}
