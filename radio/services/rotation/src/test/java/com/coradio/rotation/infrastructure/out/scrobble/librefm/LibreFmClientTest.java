package com.coradio.rotation.infrastructure.out.scrobble.librefm;

import com.coradio.rotation.domain.enums.JobStatus;
import com.coradio.rotation.domain.enums.ScrobblerProvider;
import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.model.ScrobbleJobItem;
import com.coradio.rotation.infrastructure.exception.ScrobblerBadSessionException;
import com.coradio.rotation.infrastructure.out.scrobble.librefm.dto.LibreFmSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibreFmClientTest {

    @Mock
    private LibreFmAuthenticator authenticator;

    @Mock
    private LibreFmApiClient apiClient;

    private LibreFmClient client;

    private LibreFmSession session;

    @BeforeEach
    void setUp() {
        client = new LibreFmClient(authenticator, apiClient);

        session = new LibreFmSession(
                "session",
                URI.create("https://libre.fm/1.x/nowplaying/1.2/"),
                URI.create("https://libre.fm/1.x/submissions/1.2/")
        );
    }

    @Test
    void shouldUseExistingSessionForNowPlaying() {
        PlaybackHistoryItem historyItem = playback();

        when(authenticator.currentSession()).thenReturn(session);

        client.updateNowPlaying(historyItem);

        verify(apiClient).execute(
                eq(session.nowPlayingUrl()),
                any()
        );

        verify(authenticator, never()).authenticate();
        verify(authenticator, never()).invalidate();
    }

    @Test
    void shouldAuthenticateWhenSessionMissingForNowPlaying() {
        PlaybackHistoryItem historyItem = playback();

        when(authenticator.currentSession()).thenReturn(null);
        when(authenticator.authenticate()).thenReturn(session);

        client.updateNowPlaying(historyItem);

        verify(authenticator).authenticate();

        verify(apiClient).execute(
                eq(session.nowPlayingUrl()),
                any()
        );
    }

    @Test
    void shouldReauthenticateWhenSessionExpiredForNowPlaying() {
        PlaybackHistoryItem historyItem = playback();

        LibreFmSession newSession = new LibreFmSession(
                "new-session",
                URI.create("https://libre.fm/1.x/nowplaying/1.2/"),
                URI.create("https://libre.fm/1.x/submissions/1.2/")
        );

        when(authenticator.currentSession()).thenReturn(session);
        when(authenticator.authenticate()).thenReturn(newSession);

        doThrow(new ScrobblerBadSessionException("Session expired"))
                .doNothing()
                .when(apiClient)
                .execute(any(), any());

        client.updateNowPlaying(historyItem);

        verify(authenticator).invalidate();
        verify(authenticator).authenticate();

        verify(apiClient, times(2)).execute(eq(session.nowPlayingUrl()), any());
    }

    @Test
    void shouldUseExistingSessionForScrobble() {
        ScrobbleJobItem jobItem = scrobbleItem();

        when(authenticator.currentSession()).thenReturn(session);

        client.scrobble(jobItem);

        verify(apiClient).execute(
                eq(session.submissionUrl()),
                any()
        );

        verify(authenticator, never()).authenticate();
        verify(authenticator, never()).invalidate();
    }

    @Test
    void shouldAuthenticateWhenSessionMissingForScrobble() {
        ScrobbleJobItem jobItem = scrobbleItem();

        when(authenticator.currentSession()).thenReturn(null);
        when(authenticator.authenticate()).thenReturn(session);

        client.scrobble(jobItem);

        verify(authenticator).authenticate();

        verify(apiClient).execute(
                eq(session.submissionUrl()),
                any()
        );
    }

    @Test
    void shouldReauthenticateWhenSessionExpiredForScrobble() {
        ScrobbleJobItem jobItem = scrobbleItem();

        LibreFmSession newSession = new LibreFmSession(
                "new-session",
                URI.create("https://libre.fm/1.x/nowplaying/1.2/"),
                URI.create("https://libre.fm/1.x/submissions/1.2/")
        );

        when(authenticator.currentSession()).thenReturn(session);
        when(authenticator.authenticate()).thenReturn(newSession);

        doThrow(new ScrobblerBadSessionException("Session expired"))
                .doNothing()
                .when(apiClient)
                .execute(any(), any());

        client.scrobble(jobItem);

        verify(authenticator).invalidate();
        verify(authenticator).authenticate();

        verify(apiClient, times(2)).execute(eq(session.submissionUrl()), any());
    }

    private ScrobbleJobItem scrobbleItem() {
        return new ScrobbleJobItem(
                UUID.randomUUID(),
                playback(),
                ScrobblerProvider.LIBREFM,
                JobStatus.CREATED,
                Instant.now(),
                null,
                null,
                0,
                ""
        );
    }

    private PlaybackHistoryItem playback() {
        return new PlaybackHistoryItem(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "KTRSS",
                "ATLAS",
                "Album",
                Instant.now(),
                240
        );
    }

}
