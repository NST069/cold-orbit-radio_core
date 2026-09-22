package com.coradio.rotation.application.service;

import com.coradio.rotation.application.dto.TrackInfo;
import com.coradio.rotation.application.dto.request.LiquidsoapRequest;
import com.coradio.rotation.domain.context.NowPlayingStateContext;
import com.coradio.rotation.domain.context.RecentTracksStateContext;
import com.coradio.rotation.domain.context.TrackStatsStateContext;
import com.coradio.rotation.domain.enums.NotificationEvent;
import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.model.TrackQueueItem;
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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaybackEventServiceTest {

    @Mock
    private ScrobbleService scrobbleService;

    @Mock
    private TrackQueueRepositoryPort trackQueueRepository;

    @Mock
    private TrackCatalogPort trackCatalogPort;

    @Mock
    private NowPlayingStateContext nowPlayingStateContext;

    @Mock
    private RecentTracksStateContext recentTracksStateContext;

    @Mock
    private TrackStatsStateContext trackStatsStateContext;

    @InjectMocks
    private PlaybackEventService service;

    @Test
    void shouldHandleTrackStartEvent() {

        UUID trackId = UUID.randomUUID();
        UUID queueId = UUID.randomUUID();
        Instant playedAt = Instant.now();

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
                playedAt
        );

        TrackInfo track = new TrackInfo(
                trackId,
                "KTRSS",
                "ATLAS",
                "",
                100,
                "1234.mp3"
        );

        when(trackQueueRepository.findByLocalPath(request.uri())).thenReturn(Optional.of(queueItem));
        when(trackCatalogPort.findById(trackId)).thenReturn(Optional.of(track));

        service.handleLiquidsoapEvent(request);

        verify(trackQueueRepository).markPlaying(queueId);
        verify(scrobbleService).publish(NotificationEvent.NOW_PLAYING, trackId, track.artist(), track.title(), track.album(), track.duration(), playedAt);
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

        UUID trackId = UUID.randomUUID();
        Instant playedAt = Instant.now();

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

        TrackInfo track = new TrackInfo(
                trackId,
                "KTRSS",
                "ATLAS",
                "",
                100,
                "1234.mp3"
        );

        when(nowPlayingStateContext.getCurrentTrack()).thenReturn(track);
        when(nowPlayingStateContext.getStartedAt()).thenReturn(playedAt);

        service.handleLiquidsoapEvent(request);

        verify(scrobbleService, times(1)).publish(NotificationEvent.SCROBBLE, track.id(), track.artist(), track.title(), track.album(), track.duration(), playedAt);
    }

}
