package com.coradio.rotation.application.service;

import com.coradio.rotation.application.dto.TrackInfo;
import com.coradio.rotation.application.dto.request.LiquidsoapRequest;
import com.coradio.rotation.application.exception.HistoryItemNotFoundException;
import com.coradio.rotation.application.exception.QueueItemNotFoundException;
import com.coradio.rotation.application.exception.TrackNotFoundException;
import com.coradio.rotation.domain.context.NowPlayingStateContext;
import com.coradio.rotation.domain.enums.LiquidsoapEvent;
import com.coradio.rotation.domain.enums.NotificationEvent;
import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.domain.port.in.PlaybackEventUseCase;
import com.coradio.rotation.domain.port.out.liquidsoap.PlaybackEnginePort;
import com.coradio.rotation.domain.port.out.persistence.PlaybackHistoryRepositoryPort;
import com.coradio.rotation.domain.port.out.persistence.TrackCatalogPort;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaybackEventService implements PlaybackEventUseCase {

    private final PlaybackHistoryRepositoryPort playbackHistoryRepository;

    private final TrackQueueRepositoryPort trackQueueRepository;

    private final TrackCatalogPort trackCatalogPort;

    private final PlaybackEnginePort playbackEngine;

    private final ScrobbleService scrobbleService;

    private final NowPlayingStateContext nowPlayingStateContext;

    @Override
    public void handleLiquidsoapEvent(LiquidsoapRequest request) {

        LiquidsoapEvent event = LiquidsoapEvent.fromValue(request.event());

        log.info("Incoming Liquidsoap Event: {}", event.getValue());

        switch (event) {
            case LiquidsoapEvent.TRACK_START:
                handleTrackStartEvent(request);
                break;
            case LiquidsoapEvent.TRACK_END:
                handleTrackEndEvent(request);
                break;
            case LiquidsoapEvent.TRACK_SCROBBLE:
                handleTrackScrobbleEvent(request);
                break;
            default:
                log.warn("Event not presented: {}", event);
        }

    }

    private void handleTrackStartEvent(LiquidsoapRequest request) {
        String currentTrack = request.uri();

        TrackQueueItem queueItem = trackQueueRepository.findByLocalPath(currentTrack)
                .orElseThrow(() -> new QueueItemNotFoundException(currentTrack));

        TrackInfo trackInfo = trackCatalogPort.findById(queueItem.trackId())
                .orElseThrow(() -> new TrackNotFoundException(queueItem.trackId().toString()));

        trackQueueRepository.markPlaying(queueItem.id());

        nowPlayingStateContext.setCurrentQueueItem(queueItem);
        nowPlayingStateContext.setCurrentTrack(trackInfo);

        log.debug("Track playing {}", queueItem.trackId());

        PlaybackHistoryItem historyItem = createPlaybackHistory(queueItem);

        scrobbleService.publish(NotificationEvent.NOW_PLAYING,
                trackInfo.id(),
                trackInfo.artist(),
                trackInfo.title(),
                trackInfo.album(),
                trackInfo.duration(),
                historyItem.playedAt());
    }

    private void handleTrackEndEvent(LiquidsoapRequest request) {
        String currentTrack = request.uri();

        TrackQueueItem queueItem = trackQueueRepository.findByLocalPath(currentTrack)
                .orElseThrow(() -> new QueueItemNotFoundException(currentTrack));

        trackQueueRepository.markPlayed(queueItem.id());

        log.debug("Track played {}", queueItem.trackId());
    }

    private void handleTrackScrobbleEvent(LiquidsoapRequest request) {
        String artist = request.artist();
        String title = request.title();
        String album = request.album();
        String duration = request.duration();

        PlaybackHistoryItem historyItem = playbackHistoryRepository.findLatestByArtistAndTitle(artist, title)
                .orElseThrow(() -> new HistoryItemNotFoundException(artist + " - " + title));

        log.debug("Publishing scrobbler event for {}", historyItem.trackId());
        scrobbleService.publish(NotificationEvent.SCROBBLE,
                historyItem.trackId(),
                artist,
                title,
                album,
                Long.getLong(duration),
                historyItem.playedAt());
    }

    private PlaybackHistoryItem createPlaybackHistory(TrackQueueItem queueItem) {
        TrackInfo track = trackCatalogPort.findById(queueItem.trackId())
                .orElseThrow(() -> new TrackNotFoundException(queueItem.trackId().toString()));
        int duration = Math.round(Float.parseFloat(playbackEngine.getCurrentTrackDuration()
                .orElse("0")
        ));
        PlaybackHistoryItem historyItem = new PlaybackHistoryItem(
                null,
                queueItem.trackId(),
                track.artist(),
                track.title(),
                "",
                Instant.now(),
                duration
        );
        return playbackHistoryRepository.save(historyItem);
    }

}
