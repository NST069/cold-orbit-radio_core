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
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class LibreFmClientIntegrationTest {

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

        LibreFmResponseParser responseParser = new LibreFmResponseParser();

        LibreFmApiClient apiClient = new LibreFmApiClient(
                restClient,
                responseParser
        );

        client = new LibreFmClient(
                authenticator,
                apiClient
        );

        session = new LibreFmSession(
                "session",
                URI.create("https://libre.fm/1.x/nowplaying/1.2/"),
                URI.create("https://libre.fm/1.x/submissions/1.2/")
        );
    }

    @Test
    void shouldSendNowPlayingRequest() {
        PlaybackHistoryItem historyItem = playback();

        when(authenticator.currentSession()).thenReturn(session);

        server.expect(requestTo(session.nowPlayingUrl()))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string(containsString("s=session")))
                .andExpect(content().string(containsString("a=KTRSS")))
                .andExpect(content().string(containsString("t=ATLAS")))
                .andExpect(content().string(containsString("l=240")))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        client.updateNowPlaying(historyItem);

        verify(authenticator, never()).authenticate();
        verify(authenticator, never()).invalidate();

        server.verify();
    }

    @Test
    void shouldSendScrobbleRequest() {
        ScrobbleJobItem jobItem = scrobbleItem();

        when(authenticator.currentSession()).thenReturn(session);

        server.expect(requestTo(session.submissionUrl()))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string(containsString("s=session")))
                .andExpect(content().string(containsString("a%5B0%5D=KTRSS")))
                .andExpect(content().string(containsString("t%5B0%5D=ATLAS")))
                .andExpect(content().string(containsString("o%5B0%5D=P")))
                .andExpect(content().string(containsString("l%5B0%5D=240")))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        client.scrobble(jobItem);

        verify(authenticator, never()).authenticate();
        verify(authenticator, never()).invalidate();

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
                Instant.ofEpochSecond(1_000),
                240
        );
    }
}
