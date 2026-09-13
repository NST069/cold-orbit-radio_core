package com.coradio.rotation.application.service;

import com.coradio.rotation.application.dto.TrackInfo;
import com.coradio.rotation.application.dto.request.LiquidsoapRequest;
import com.coradio.rotation.domain.context.NowPlayingStateContext;
import com.coradio.rotation.domain.enums.NotificationEvent;
import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.model.TrackQueueItem;
import com.coradio.rotation.domain.port.out.liquidsoap.PlaybackEnginePort;
import com.coradio.rotation.domain.port.out.persistence.PlaybackHistoryRepositoryPort;
import com.coradio.rotation.domain.port.out.persistence.TrackCatalogPort;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaybackEventServiceTest {

    @Mock
    private PlaybackHistoryRepositoryPort playbackHistoryRepository;

    @Mock
    private ScrobbleService scrobbleService;

    @Mock
    private TrackQueueRepositoryPort trackQueueRepository;

    @Mock
    private TrackCatalogPort trackCatalogPort;

    @Mock
    private PlaybackEnginePort playbackEngine;

    @Mock
    private NowPlayingStateContext nowPlayingStateContext;

    @InjectMocks
    private PlaybackEventService service;

    @Test
    void shouldHandleTrackStartEvent() {

        UUID trackId = UUID.randomUUID();
        UUID queueId = UUID.randomUUID();

        LiquidsoapRequest request = new LiquidsoapRequest(
                "track_start",
                1234567L,
                "KTRSS",
                "ATLAS",
                "",
                "100",
                "",
                "/app/music/test.mp3"
        );


        TrackQueueItem queueItem = new TrackQueueItem(
                queueId,
                trackId,
                PlaybackStatus.QUEUED,
                "/app/music/test.mp3",
                Instant.now(),
                Instant.now()
        );

        TrackInfo track = new TrackInfo(
                trackId,
                "KTRSS",
                "ATLAS",
                "",
                100,
                "1234.mp3"
        );

        PlaybackHistoryItem history = new PlaybackHistoryItem(
                UUID.randomUUID(),
                trackId,
                "KTRSS",
                "ATLAS",
                "",
                Instant.now(),
                120
        );

        when(trackQueueRepository.findByLocalPath(request.uri())).thenReturn(Optional.of(queueItem));
        when(trackCatalogPort.findById(trackId)).thenReturn(Optional.of(track));
        when(playbackEngine.getCurrentTrackDuration()).thenReturn(Optional.of("120"));
        when(playbackHistoryRepository.save(any())).thenReturn(history);

        service.handleLiquidsoapEvent(request);

        verify(trackQueueRepository).markPlaying(queueId);
        verify(playbackHistoryRepository).save(any(PlaybackHistoryItem.class));
        verify(scrobbleService).publish(NotificationEvent.NOW_PLAYING, trackId, track.artist(), track.title(), track.album(), track.duration(), history.playedAt());
    }

    @Test
    void shouldHandleTrackEndEvent() {

        UUID queueId = UUID.randomUUID();

        LiquidsoapRequest request = new LiquidsoapRequest(
                "track_end",
                1234567L,
                "KTRSS",
                "ATLAS",
                "",
                "100",
                "",
                "/app/music/test.mp3"
        );

        TrackQueueItem queueItem = new TrackQueueItem(
                queueId,
                UUID.randomUUID(),
                PlaybackStatus.PLAYING,
                "/app/music/test.mp3",
                Instant.now(),
                Instant.now()
        );

        when(trackQueueRepository.findByLocalPath(request.uri())).thenReturn(Optional.of(queueItem));

        service.handleLiquidsoapEvent(request);

        verify(trackQueueRepository).markPlayed(queueId);
    }

    @Test
    void shouldSendScrobbleEventOnTrackScrobble() {

        LiquidsoapRequest request = new LiquidsoapRequest(
                "track_scrobble",
                123456L,
                "KTRSS",
                "ATLAS",
                "",
                "100",
                "",
                "/app/music/test.mp3"
        );

        PlaybackHistoryItem history = new PlaybackHistoryItem(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "KTRSS",
                "ATLAS",
                "",
                Instant.now(),
                100
        );

        when(playbackHistoryRepository.findLatestByArtistAndTitle("KTRSS", "ATLAS")).thenReturn(Optional.of(history));

        service.handleLiquidsoapEvent(request);

        verify(scrobbleService, times(1)).publish(NotificationEvent.SCROBBLE, history.trackId(), history.artist(), history.title(), history.album(), history.duration(), history.playedAt());
    }

}
