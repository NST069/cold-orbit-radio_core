package com.coradio.rotation.infrastructure.out.persistense.adapter;

import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import com.coradio.rotation.infrastructure.out.persistense.entity.TrackQueueEntity;
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

    @Override
    public int countQueued() {
        return trackQueueRepository.countByStatus(PlaybackStatus.QUEUED);
    }

    @Override
    public List<UUID> findActiveTrackIds() {
        return trackQueueRepository.findAll().stream()
                .filter(track -> !List.of(PlaybackStatus.FAILED, PlaybackStatus.PLAYED).contains(track.getStatus()))
                .map(TrackQueueEntity::getTrackId)
                .toList();
    }

    @Override
    public void markDownloading(UUID id) {
        trackQueueRepository.markDownloading(id);
    }

    @Override
    public void markFailed(UUID id, String reason) {
        trackQueueRepository.markFailed(id, reason);
    }

    @Override
    public void markQueued(UUID id) {
        trackQueueRepository.markQueued(id);
    }

    @Override
    public void markReady(UUID id, String localPath) {
        trackQueueRepository.markReady(id, localPath);
    }

    @Override
    public void markPlaying(UUID id) {
        trackQueueRepository.markPlaying(id);
    }

    @Override
    public void markPlayed(UUID id) {
        trackQueueRepository.markPlayed(id);
    }

    @Override
    public List<TrackQueueItem> findReadyTracks(int limit) {
        return trackQueueRepository.findByStatusOrderByCreatedAtAsc(PlaybackStatus.READY).stream()
                .limit(limit)
                .map(TrackQueueMapper::toDomain).toList();
    }

    @Override
    public Optional<TrackQueueItem> findByLocalPath(String localPath) {
        return trackQueueRepository.findByLocalPath(localPath);
    }

    @Override
    public Optional<TrackQueueItem> findPlayingTrack() {
        return trackQueueRepository.findByStatus(PlaybackStatus.PLAYING);
    }
}
