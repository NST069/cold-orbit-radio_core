package com.coradio.rotation.infrastructure.out.scrobble.librefm;

import com.coradio.rotation.domain.enums.JobStatus;
import com.coradio.rotation.domain.enums.ScrobblerProvider;
import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.model.ScrobbleJobItem;
import com.coradio.rotation.infrastructure.out.scrobble.librefm.dto.LibreFmSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class LibreFmClientTest {

    private MockRestServiceServer server;

    @Mock
    private LibreFmAuthenticator authenticator;

    private LibreFmClient client;

    private LibreFmSession session;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();

        server = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder.build();

        client = new LibreFmClient(restClient, authenticator);

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

        server.expect(ExpectedCount.once(),
                        requestTo(session.nowPlayingUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        client.updateNowPlaying(historyItem);

        verify(authenticator, never()).authenticate();
        verify(authenticator, never()).invalidate();

        server.verify();
    }

    @Test
    void shouldAuthenticateWhenSessionMissingForNowPlaying() {
        PlaybackHistoryItem historyItem = playback();

        when(authenticator.currentSession()).thenReturn(null);
        when(authenticator.authenticate()).thenReturn(session);

        server.expect(ExpectedCount.once(),
                        requestTo(session.nowPlayingUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        client.updateNowPlaying(historyItem);

        verify(authenticator).authenticate();

        server.verify();
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

        server.expect(ExpectedCount.once(),
                        requestTo(session.nowPlayingUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("BADSESSION", MediaType.TEXT_PLAIN));

        server.expect(ExpectedCount.once(),
                        requestTo(newSession.nowPlayingUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        client.updateNowPlaying(historyItem);

        verify(authenticator, times(1)).invalidate();
        verify(authenticator, times(1)).authenticate();

        server.verify();
    }

    @Test
    void shouldUseExistingSessionForScrobble() {
        ScrobbleJobItem jobItem = scrobbleItem();

        when(authenticator.currentSession()).thenReturn(session);

        server.expect(ExpectedCount.once(),
                        requestTo(session.submissionUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        client.scrobble(jobItem);

        verify(authenticator, never()).authenticate();
        verify(authenticator, never()).invalidate();

        server.verify();
    }

    @Test
    void shouldAuthenticateWhenSessionMissingForScrobble() {
        ScrobbleJobItem jobItem = scrobbleItem();

        when(authenticator.currentSession()).thenReturn(null);
        when(authenticator.authenticate()).thenReturn(session);

        server.expect(ExpectedCount.once(),
                        requestTo(session.submissionUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        client.scrobble(jobItem);

        verify(authenticator).authenticate();

        server.verify();
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

        server.expect(ExpectedCount.once(),
                        requestTo(session.submissionUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("BADSESSION", MediaType.TEXT_PLAIN));

        server.expect(ExpectedCount.once(),
                        requestTo(newSession.submissionUrl()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        client.scrobble(jobItem);

        verify(authenticator, times(1)).invalidate();
        verify(authenticator, times(1)).authenticate();

        server.verify();
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
