package com.coradio.rotation.application.service;

import com.coradio.rotation.application.dto.TrackInfo;
import com.coradio.rotation.application.dto.request.LiquidsoapRequest;
import com.coradio.rotation.application.exception.QueueItemNotFoundException;
import com.coradio.rotation.application.exception.TrackNotFoundException;
import com.coradio.rotation.domain.context.NowPlayingStateContext;
import com.coradio.rotation.domain.context.RecentTracksStateContext;
import com.coradio.rotation.domain.context.TrackStatsStateContext;
import com.coradio.rotation.domain.enums.LiquidsoapEvent;
import com.coradio.rotation.domain.enums.NotificationEvent;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.domain.port.in.PlaybackEventUseCase;
import com.coradio.rotation.domain.port.out.persistence.TrackCatalogPort;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaybackEventService implements PlaybackEventUseCase {

    private final TrackQueueRepositoryPort trackQueueRepository;

    private final TrackCatalogPort trackCatalogPort;

    private final ScrobbleService scrobbleService;

    private final NowPlayingStateContext nowPlayingStateContext;

    private final RecentTracksStateContext recentTracksStateContext;

    private final TrackStatsStateContext trackStatsStateContext;

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

        nowPlayingStateContext.set(trackInfo);

        log.debug("Track playing {}", queueItem.trackId());

        recentTracksStateContext.add(trackInfo.id(), trackInfo.artist(), trackInfo.title(), queueItem.playedAt());
        trackStatsStateContext.incrementPlayCount(trackInfo.id());

        scrobbleService.publish(NotificationEvent.NOW_PLAYING,
                trackInfo.id(),
                trackInfo.artist(),
                trackInfo.title(),
                trackInfo.album(),
                trackInfo.duration(),
                queueItem.playedAt());
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

        log.debug("Publishing scrobbler event for {}", nowPlayingStateContext.getCurrentTrack().id());
        scrobbleService.publish(NotificationEvent.SCROBBLE,
                nowPlayingStateContext.getCurrentTrack().id(),
                artist,
                title,
                album,
                duration.isBlank() ? nowPlayingStateContext.getCurrentTrack().duration() : Long.parseLong(duration),
                nowPlayingStateContext.getStartedAt());
    }

}
