package com.coradio.rotation.infrastructure.out.scrobble.lastfm;

import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.model.ScrobbleJobItem;
import com.coradio.rotation.infrastructure.exception.ScrobblerBadSessionException;
import com.coradio.rotation.infrastructure.out.scrobble.lastfm.dto.LastFmSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMap;
import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastFmClientTest {

    @Mock
    private LastFmAuthenticator authenticator;

    @Mock
    private LastFmApiClient apiClient;

    @Mock
    private LastFmApiSigner signer;

    @Mock
    private LastFmProperties properties;

    @Mock
    private PlaybackHistoryItem historyItem;

    @Mock
    private ScrobbleJobItem jobItem;

    @Mock
    private LastFmSession session;

    @Mock
    private LastFmSession newSession;

    private LastFmClient client;

    @BeforeEach
    void setUp() {
        client = new LastFmClient(
                authenticator,
                apiClient,
                signer,
                properties
        );

        when(properties.apiUrl()).thenReturn("https://ws.audioscrobbler.com/2.0/");
        when(properties.apiKey()).thenReturn("api-key");
        when(properties.apiSecret()).thenReturn("api-secret");

        when(session.sessionKey()).thenReturn("session-key");

        when(signer.sign(anyMap(), eq("api-secret"))).thenReturn("signature");
    }

    @Test
    void updateNowPlaying_shouldAuthenticateWhenSessionMissing() {
        when(authenticator.currentSession()).thenReturn(null);
        when(authenticator.authenticate()).thenReturn(session);

        client.updateNowPlaying(historyItem);

        verify(authenticator).authenticate();
        verify(apiClient).execute(
                eq(URI.create("https://ws.audioscrobbler.com/2.0/")),
                any()
        );
    }

    @Test
    void updateNowPlaying_shouldUseCurrentSession() {
        when(authenticator.currentSession()).thenReturn(session);

        client.updateNowPlaying(historyItem);

        verify(authenticator, never()).authenticate();

        verify(apiClient).execute(
                eq(URI.create("https://ws.audioscrobbler.com/2.0/")),
                any()
        );
    }

    @Test
    void updateNowPlaying_shouldSendCorrectParams() {
        when(authenticator.currentSession()).thenReturn(session);

        when(historyItem.artist()).thenReturn("Artist");
        when(historyItem.title()).thenReturn("Track");
        when(historyItem.duration()).thenReturn(180);
        when(historyItem.album()).thenReturn("Album");

        ArgumentCaptor<MultiValueMap<String, String>> captor = ArgumentCaptor.forClass(MultiValueMap.class);

        client.updateNowPlaying(historyItem);

        verify(apiClient).execute(
                eq(URI.create("https://ws.audioscrobbler.com/2.0/")),
                captor.capture()
        );

        MultiValueMap<String, String> form = captor.getValue();

        assertThat(form.getFirst("method")).isEqualTo("track.updateNowPlaying");
        assertThat(form.getFirst("api_key")).isEqualTo("api-key");
        assertThat(form.getFirst("sk")).isEqualTo("session-key");
        assertThat(form.getFirst("artist")).isEqualTo("Artist");
        assertThat(form.getFirst("track")).isEqualTo("Track");
        assertThat(form.getFirst("duration")).isEqualTo("180");
        assertThat(form.getFirst("album")).isEqualTo("Album");
        assertThat(form.getFirst("api_sig")).isEqualTo("signature");
        assertThat(form.getFirst("format")).isEqualTo("json");
    }

    @Test
    void updateNowPlaying_shouldNotSendAlbumWhenAlbumIsNull() {
        when(authenticator.currentSession()).thenReturn(session);
        when(historyItem.album()).thenReturn(null);

        ArgumentCaptor<MultiValueMap<String, String>> captor =
                ArgumentCaptor.forClass(MultiValueMap.class);

        client.updateNowPlaying(historyItem);

        verify(apiClient).execute(any(), captor.capture());

        assertThat(captor.getValue()).doesNotContainKey("album");
    }

    @Test
    void updateNowPlaying_shouldRetryWithNewSessionWhenSessionIsInvalid() {
        when(newSession.sessionKey()).thenReturn("new-session-key");
        when(authenticator.currentSession()).thenReturn(session);
        when(authenticator.authenticate()).thenReturn(newSession);

        when(historyItem.artist()).thenReturn("Artist");
        when(historyItem.title()).thenReturn("Track");
        when(historyItem.duration()).thenReturn(180);
        when(historyItem.album()).thenReturn("Album");

        doThrow(new ScrobblerBadSessionException("Bad Session"))
                .doNothing()
                .when(apiClient)
                .execute(any(), any());

        ArgumentCaptor<MultiValueMap<String, String>> captor = ArgumentCaptor.forClass(MultiValueMap.class);

        client.updateNowPlaying(historyItem);

        verify(authenticator).invalidate();
        verify(authenticator).authenticate();

        verify(apiClient, times(2))
                .execute(
                        eq(URI.create("https://ws.audioscrobbler.com/2.0/")),
                        captor.capture()
                );

        List<MultiValueMap<String, String>> requests = captor.getAllValues();

        assertThat(requests.get(0).getFirst("sk")).isEqualTo("session-key");

        assertThat(requests.get(1).getFirst("sk")).isEqualTo("new-session-key");
    }

    @Test
    void scrobble_shouldAuthenticateWhenSessionMissing() {
        when(authenticator.currentSession()).thenReturn(null);
        when(authenticator.authenticate()).thenReturn(session);
        when(jobItem.playbackHistoryItem()).thenReturn(historyItem);

        when(historyItem.artist()).thenReturn("Artist");
        when(historyItem.title()).thenReturn("Track");
        when(historyItem.duration()).thenReturn(180);
        when(historyItem.album()).thenReturn("Album");
        when(historyItem.playedAt()).thenReturn(Instant.ofEpochSecond(1234567890L));

        client.scrobble(jobItem);

        verify(authenticator).authenticate();
        verify(apiClient).execute(
                eq(URI.create("https://ws.audioscrobbler.com/2.0/")),
                any()
        );
    }

    @Test
    void scrobble_shouldUseCurrentSession() {
        when(authenticator.currentSession()).thenReturn(session);
        when(jobItem.playbackHistoryItem()).thenReturn(historyItem);

        when(historyItem.artist()).thenReturn("Artist");
        when(historyItem.title()).thenReturn("Track");
        when(historyItem.duration()).thenReturn(180);
        when(historyItem.album()).thenReturn("Album");
        when(historyItem.playedAt()).thenReturn(Instant.ofEpochSecond(1234567890L));

        client.scrobble(jobItem);

        verify(authenticator, never()).authenticate();

        verify(apiClient).execute(
                eq(URI.create("https://ws.audioscrobbler.com/2.0/")),
                any()
        );
    }

    @Test
    void scrobble_shouldSendCorrectParams() {
        when(authenticator.currentSession()).thenReturn(session);
        when(jobItem.playbackHistoryItem()).thenReturn(historyItem);

        when(historyItem.artist()).thenReturn("Artist");
        when(historyItem.title()).thenReturn("Track");
        when(historyItem.duration()).thenReturn(180);
        when(historyItem.album()).thenReturn("Album");
        when(historyItem.playedAt()).thenReturn(Instant.ofEpochSecond(1234567890L));

        ArgumentCaptor<MultiValueMap<String, String>> captor = ArgumentCaptor.forClass(MultiValueMap.class);

        client.scrobble(jobItem);

        verify(apiClient).execute(
                eq(URI.create("https://ws.audioscrobbler.com/2.0/")),
                captor.capture()
        );

        MultiValueMap<String, String> form = captor.getValue();

        assertThat(form.getFirst("method")).isEqualTo("track.scrobble");
        assertThat(form.getFirst("api_key")).isEqualTo("api-key");
        assertThat(form.getFirst("sk")).isEqualTo("session-key");
        assertThat(form.getFirst("artist")).isEqualTo("Artist");
        assertThat(form.getFirst("track")).isEqualTo("Track");
        assertThat(form.getFirst("timestamp")).isEqualTo("1234567890");
        assertThat(form.getFirst("duration")).isEqualTo("180");
        assertThat(form.getFirst("album")).isEqualTo("Album");
        assertThat(form.getFirst("api_sig")).isEqualTo("signature");
        assertThat(form.getFirst("format")).isEqualTo("json");
    }

    @Test
    void scrobble_shouldNotSendAlbumWhenAlbumIsNull() {
        when(authenticator.currentSession()).thenReturn(session);
        when(jobItem.playbackHistoryItem()).thenReturn(historyItem);
        when(historyItem.album()).thenReturn(null);

        when(historyItem.artist()).thenReturn("Artist");
        when(historyItem.title()).thenReturn("Track");
        when(historyItem.duration()).thenReturn(180);
        when(historyItem.album()).thenReturn(null);
        when(historyItem.playedAt()).thenReturn(Instant.ofEpochSecond(1234567890L));

        ArgumentCaptor<MultiValueMap<String, String>> captor = ArgumentCaptor.forClass(MultiValueMap.class);

        client.scrobble(jobItem);

        verify(apiClient).execute(any(), captor.capture());

        assertThat(captor.getValue()).doesNotContainKey("album");
    }

    @Test
    void scrobble_shouldRetryWithNewSessionWhenSessionIsInvalid() {
        when(newSession.sessionKey()).thenReturn("new-session-key");
        when(authenticator.currentSession()).thenReturn(session);
        when(authenticator.authenticate()).thenReturn(newSession);
        when(jobItem.playbackHistoryItem()).thenReturn(historyItem);

        when(historyItem.playedAt()).thenReturn(Instant.ofEpochSecond(1234567890L));

        doThrow(new ScrobblerBadSessionException("Bad Session"))
                .doNothing()
                .when(apiClient)
                .execute(any(), any());

        ArgumentCaptor<MultiValueMap<String, String>> captor = ArgumentCaptor.forClass(MultiValueMap.class);

        client.scrobble(jobItem);

        verify(authenticator).invalidate();
        verify(authenticator).authenticate();

        verify(apiClient, times(2))
                .execute(
                        eq(URI.create("https://ws.audioscrobbler.com/2.0/")),
                        captor.capture()
                );

        List<MultiValueMap<String, String>> requests = captor.getAllValues();

        assertThat(requests.get(0).getFirst("sk")).isEqualTo("session-key");
        assertThat(requests.get(1).getFirst("sk")).isEqualTo("new-session-key");
    }
}
