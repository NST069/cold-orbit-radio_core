package com.coradio.notification.infrastructure.out.scrobble.lastfm;

import com.coradio.notification.domain.model.StreamEvent;
import com.coradio.notification.domain.model.ScrobbleTrack;
import com.coradio.notification.infrastructure.out.scrobble.lastfm.dto.LastFmSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMap;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
    private ScrobbleTrack scrobbleTrack;

    @Mock
    private StreamEvent scrobbleEvent;

    @Mock
    private LastFmSession session;

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

        client.updateNowPlaying(scrobbleTrack);

        verify(authenticator).authenticate();
        verify(apiClient).execute(
                eq(URI.create("https://ws.audioscrobbler.com/2.0/")),
                any()
        );
    }

    @Test
    void updateNowPlaying_shouldUseCurrentSession() {
        when(authenticator.currentSession()).thenReturn(session);

        client.updateNowPlaying(scrobbleTrack);

        verify(authenticator, never()).authenticate();

        verify(apiClient).execute(
                eq(URI.create("https://ws.audioscrobbler.com/2.0/")),
                any()
        );
    }

    @Test
    void updateNowPlaying_shouldSendCorrectParams() {
        when(authenticator.currentSession()).thenReturn(session);

        when(scrobbleTrack.artist()).thenReturn("Artist");
        when(scrobbleTrack.title()).thenReturn("Track");
        when(scrobbleTrack.duration()).thenReturn(180L);
        when(scrobbleTrack.album()).thenReturn("Album");

        ArgumentCaptor<MultiValueMap<String, String>> captor = ArgumentCaptor.forClass(MultiValueMap.class);

        client.updateNowPlaying(scrobbleTrack);

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
        when(scrobbleTrack.album()).thenReturn(null);

        ArgumentCaptor<MultiValueMap<String, String>> captor =
                ArgumentCaptor.forClass(MultiValueMap.class);

        client.updateNowPlaying(scrobbleTrack);

        verify(apiClient).execute(any(), captor.capture());

        assertThat(captor.getValue()).doesNotContainKey("album");
    }

    @Test
    void scrobble_shouldAuthenticateWhenSessionMissing() {
        when(authenticator.currentSession()).thenReturn(null);
        when(authenticator.authenticate()).thenReturn(session);

        when(scrobbleTrack.artist()).thenReturn("Artist");
        when(scrobbleTrack.title()).thenReturn("Track");
        when(scrobbleTrack.duration()).thenReturn(180L);
        when(scrobbleTrack.album()).thenReturn("Album");
        when(scrobbleEvent.playedAt()).thenReturn(1234567890L);

        client.scrobble(scrobbleTrack, scrobbleEvent.playedAt());

        verify(authenticator).authenticate();
        verify(apiClient).execute(
                eq(URI.create("https://ws.audioscrobbler.com/2.0/")),
                any()
        );
    }

    @Test
    void scrobble_shouldUseCurrentSession() {
        when(authenticator.currentSession()).thenReturn(session);

        when(scrobbleTrack.artist()).thenReturn("Artist");
        when(scrobbleTrack.title()).thenReturn("Track");
        when(scrobbleTrack.duration()).thenReturn(180L);
        when(scrobbleTrack.album()).thenReturn("Album");
        when(scrobbleEvent.playedAt()).thenReturn(1234567890L);

        client.scrobble(scrobbleTrack, scrobbleEvent.playedAt());

        verify(authenticator, never()).authenticate();

        verify(apiClient).execute(
                eq(URI.create("https://ws.audioscrobbler.com/2.0/")),
                any()
        );
    }

    @Test
    void scrobble_shouldSendCorrectParams() {
        when(authenticator.currentSession()).thenReturn(session);

        when(scrobbleTrack.artist()).thenReturn("Artist");
        when(scrobbleTrack.title()).thenReturn("Track");
        when(scrobbleTrack.duration()).thenReturn(180L);
        when(scrobbleTrack.album()).thenReturn("Album");
        when(scrobbleEvent.playedAt()).thenReturn(1234567890L);

        ArgumentCaptor<MultiValueMap<String, String>> captor = ArgumentCaptor.forClass(MultiValueMap.class);

        client.scrobble(scrobbleTrack, scrobbleEvent.playedAt());

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

        when(scrobbleTrack.artist()).thenReturn("Artist");
        when(scrobbleTrack.title()).thenReturn("Track");
        when(scrobbleTrack.duration()).thenReturn(180L);
        when(scrobbleTrack.album()).thenReturn(null);
        when(scrobbleEvent.playedAt()).thenReturn(1234567890L);

        ArgumentCaptor<MultiValueMap<String, String>> captor = ArgumentCaptor.forClass(MultiValueMap.class);

        client.scrobble(scrobbleTrack, scrobbleEvent.playedAt());

        verify(apiClient).execute(any(), captor.capture());

        assertThat(captor.getValue()).doesNotContainKey("album");
    }

}
