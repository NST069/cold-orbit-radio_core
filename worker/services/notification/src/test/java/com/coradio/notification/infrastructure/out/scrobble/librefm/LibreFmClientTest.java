package com.coradio.notification.infrastructure.out.scrobble.librefm;

import com.coradio.notification.domain.model.ScrobbleTrack;
import com.coradio.notification.infrastructure.out.scrobble.librefm.dto.LibreFmSession;
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
import static org.mockito.Mockito.never;
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
        ScrobbleTrack track = scrobbleTrack();

        when(authenticator.currentSession()).thenReturn(session);

        client.updateNowPlaying(track);

        verify(apiClient).execute(
                eq(session.nowPlayingUrl()),
                any()
        );

        verify(authenticator, never()).authenticate();
        verify(authenticator, never()).invalidate();
    }

    @Test
    void shouldAuthenticateWhenSessionMissingForNowPlaying() {
        ScrobbleTrack track = scrobbleTrack();

        when(authenticator.currentSession()).thenReturn(null);
        when(authenticator.authenticate()).thenReturn(session);

        client.updateNowPlaying(track);

        verify(authenticator).authenticate();

        verify(apiClient).execute(
                eq(session.nowPlayingUrl()),
                any()
        );
    }

    @Test
    void shouldUseExistingSessionForScrobble() {
        ScrobbleTrack track = scrobbleTrack();

        when(authenticator.currentSession()).thenReturn(session);

        client.scrobble(track, Instant.now().getEpochSecond());

        verify(apiClient).execute(
                eq(session.submissionUrl()),
                any()
        );

        verify(authenticator, never()).authenticate();
        verify(authenticator, never()).invalidate();
    }

    @Test
    void shouldAuthenticateWhenSessionMissingForScrobble() {
        ScrobbleTrack track = scrobbleTrack();

        when(authenticator.currentSession()).thenReturn(null);
        when(authenticator.authenticate()).thenReturn(session);

        client.scrobble(track, Instant.now().getEpochSecond());

        verify(authenticator).authenticate();

        verify(apiClient).execute(
                eq(session.submissionUrl()),
                any()
        );
    }

    private ScrobbleTrack scrobbleTrack() {
        return new ScrobbleTrack(
                UUID.randomUUID(),
                "KTRSS",
                "ATLAS",
                "Album",
                240
        );
    }

}
