package com.coradio.rotation.application.service;

import com.coradio.rotation.application.dto.TrackInfo;
import com.coradio.rotation.application.dto.request.LiquidsoapRequest;
import com.coradio.rotation.application.exception.QueueItemNotFoundException;
import com.coradio.rotation.application.exception.TrackNotFoundException;
import com.coradio.rotation.domain.enums.JobStatus;
import com.coradio.rotation.domain.enums.LiquidsoapEvent;
import com.coradio.rotation.domain.enums.ScrobblerProvider;
import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.model.ScrobbleJobItem;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.domain.port.in.PlaybackEventUseCase;
import com.coradio.rotation.domain.port.out.persistence.PlaybackHistoryRepositoryPort;
import com.coradio.rotation.domain.port.out.persistence.ScrobbleJobRepositoryPort;
import com.coradio.rotation.domain.port.out.persistence.TrackCatalogPort;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Arrays;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaybackEventService implements PlaybackEventUseCase {

    private final PlaybackHistoryRepositoryPort playbackHistoryRepository;

    private final ScrobbleJobRepositoryPort scrobblerJobRepository;

    private final TrackQueueRepositoryPort trackQueueRepository;

    private final TrackCatalogPort trackCatalogPort;

    @Override
    public void handleLiquidsoapEvent(LiquidsoapRequest request) {

        LiquidsoapEvent event = LiquidsoapEvent.fromValue(request.event());

        log.debug("Incoming Liquidsoap Event: {}", event.getValue());

        switch (event) {
            case LiquidsoapEvent.TRACK_START:
                handleTrackStartEvent(request);
                break;
            case LiquidsoapEvent.TRACK_END:
                handleTrackEndEvent(request);
                break;
            default:
                log.warn("Event not presented: {}", event);
        }

    }

    private void handleTrackStartEvent(LiquidsoapRequest request) {
        String currentTrack = request.uri();

        TrackQueueItem queueItem = trackQueueRepository.findByLocalPath(currentTrack)
                .orElseThrow(() -> new QueueItemNotFoundException(currentTrack));

        trackQueueRepository.markPlaying(queueItem.id());

        log.debug("Track playing {}", queueItem.trackId());

        PlaybackHistoryItem historyItem = createPlaybackHistory(queueItem);

        Arrays.stream(ScrobblerProvider.values()).forEach(provider -> {
            ScrobbleJobItem scrobbleJobItem = createScrobblerJobItem(provider, historyItem);
            log.debug("Created scrobbler job({}), {}", provider, scrobbleJobItem.id());
        });
    }

    private void handleTrackEndEvent(LiquidsoapRequest request) {
        String currentTrack = request.uri();

        TrackQueueItem queueItem = trackQueueRepository.findByLocalPath(currentTrack)
                .orElseThrow(() -> new QueueItemNotFoundException(currentTrack));

        trackQueueRepository.markPlayed(queueItem.id());

        log.debug("Track played {}", queueItem.trackId());
    }

    private PlaybackHistoryItem createPlaybackHistory(TrackQueueItem queueItem) {
        TrackInfo track = trackCatalogPort.findById(queueItem.trackId())
                .orElseThrow(() -> new TrackNotFoundException(queueItem.trackId().toString()));
        PlaybackHistoryItem historyItem = new PlaybackHistoryItem(null, queueItem.trackId(), track.artist(), track.title(), Instant.now());
        return playbackHistoryRepository.save(historyItem);
    }

    private ScrobbleJobItem createScrobblerJobItem(ScrobblerProvider provider, PlaybackHistoryItem historyItem) {
        ScrobbleJobItem jobItem = new ScrobbleJobItem(
                null,
                historyItem,
                provider,
                JobStatus.CREATED,
                Instant.now().plusSeconds(30),
                null,
                0,
                null
        );
        return scrobblerJobRepository.save(jobItem);
    }

}
