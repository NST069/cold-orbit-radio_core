package com.coradio.notification.infrastructure.out.scrobble.lastfm;

import com.coradio.notification.domain.port.enums.ScrobbleResult;
import com.coradio.notification.domain.port.model.ScrobbleTrack;
import com.coradio.notification.infrastructure.exception.ScrobblerBadSessionException;
import com.coradio.notification.infrastructure.out.scrobble.lastfm.dto.LastFmSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.net.URI;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LastFmClient {

    private final LastFmAuthenticator authenticator;
    private final LastFmApiClient apiClient;
    private final LastFmApiSigner signer;
    private final LastFmProperties properties;

    public ScrobbleResult updateNowPlaying(ScrobbleTrack scrobbleTrack) {
        LastFmSession session = getOrAuthenticate();

        MultiValueMap<String, String> form =
                buildNowPlayingParams(session, scrobbleTrack);

        try {
            return apiClient.execute(URI.create(properties.apiUrl()), form);
        } catch (ScrobblerBadSessionException e) {
            authenticator.invalidate();

            session = authenticator.authenticate();
            form = buildNowPlayingParams(session, scrobbleTrack);

            return apiClient.execute(URI.create(properties.apiUrl()), form);
        }
    }

    public ScrobbleResult scrobble(ScrobbleTrack scrobbleTrack, long playedAt) {
        LastFmSession session = getOrAuthenticate();

        MultiValueMap<String, String> form = buildScrobbleParams(session, scrobbleTrack, playedAt);

        try {
            return apiClient.execute(URI.create(properties.apiUrl()), form);
        } catch (ScrobblerBadSessionException e) {
            authenticator.invalidate();

            session = authenticator.authenticate();
            form = buildScrobbleParams(session, scrobbleTrack, playedAt);

            return apiClient.execute(URI.create(properties.apiUrl()), form);
        }
    }

    private MultiValueMap<String, String> buildNowPlayingParams(
            LastFmSession session,
            ScrobbleTrack scrobbleTrack
    ) {
        LinkedMultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();

        form.add("method", "track.updateNowPlaying");
        form.add("api_key", properties.apiKey());
        form.add("sk", session.sessionKey());
        form.add("artist", scrobbleTrack.artist());
        form.add("track", scrobbleTrack.title());
        form.add("duration", String.valueOf(scrobbleTrack.duration()));

        if (scrobbleTrack.album() != null) {
            form.add("album", scrobbleTrack.album());
        }

        addApiSignature(form);

        return form;
    }

    private MultiValueMap<String, String> buildScrobbleParams(
            LastFmSession session,
            ScrobbleTrack scrobbleTrack,
            long playedAt
    ) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.add("method", "track.scrobble");
        form.add("api_key", properties.apiKey());
        form.add("sk", session.sessionKey());
        form.add("artist", scrobbleTrack.artist());
        form.add("track", scrobbleTrack.title());
        form.add("timestamp", String.valueOf(playedAt));
        form.add("duration", String.valueOf(scrobbleTrack.duration()));

        if (scrobbleTrack.album() != null) {
            form.add("album", scrobbleTrack.album());
        }

        addApiSignature(form);

        return form;
    }

    private void addApiSignature(
            LinkedMultiValueMap<String, String> form
    ) {
        Map<String, String> params = form.toSingleValueMap();

        form.add("api_sig", signer.sign(params, properties.apiSecret()));
        form.add("format", "json");
    }

    private LastFmSession getOrAuthenticate() {
        LastFmSession session = authenticator.currentSession();

        if (session == null) {
            session = authenticator.authenticate();
        }

        return session;
    }
}
