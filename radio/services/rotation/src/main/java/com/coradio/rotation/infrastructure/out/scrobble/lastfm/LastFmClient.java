package com.coradio.rotation.infrastructure.out.scrobble.lastfm;

import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.model.ScrobbleJobItem;
import com.coradio.rotation.infrastructure.exception.ScrobblerBadSessionException;
import com.coradio.rotation.infrastructure.out.scrobble.lastfm.dto.LastFmSession;
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

    public void updateNowPlaying(PlaybackHistoryItem historyItem) {
        LastFmSession session = getOrAuthenticate();

        MultiValueMap<String, String> form =
                buildNowPlayingParams(session, historyItem);

        try {
            apiClient.execute(URI.create(properties.apiUrl()), form);
        } catch (ScrobblerBadSessionException e) {
            authenticator.invalidate();

            session = authenticator.authenticate();
            form = buildNowPlayingParams(session, historyItem);

            apiClient.execute(URI.create(properties.apiUrl()), form);
        }
    }

    public void scrobble(ScrobbleJobItem jobItem) {
        LastFmSession session = getOrAuthenticate();

        PlaybackHistoryItem historyItem = jobItem.playbackHistoryItem();

        MultiValueMap<String, String> form = buildScrobbleParams(session, historyItem);

        try {
            apiClient.execute(URI.create(properties.apiUrl()), form);
        } catch (ScrobblerBadSessionException e) {
            authenticator.invalidate();

            session = authenticator.authenticate();
            form = buildScrobbleParams(session, historyItem);

            apiClient.execute(URI.create(properties.apiUrl()), form);
        }
    }

    private MultiValueMap<String, String> buildNowPlayingParams(
            LastFmSession session,
            PlaybackHistoryItem historyItem
    ) {
        LinkedMultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();

        form.add("method", "track.updateNowPlaying");
        form.add("api_key", properties.apiKey());
        form.add("sk", session.sessionKey());
        form.add("artist", historyItem.artist());
        form.add("track", historyItem.title());
        form.add("duration", String.valueOf(historyItem.duration()));

        if (historyItem.album() != null) {
            form.add("album", historyItem.album());
        }

        addApiSignature(form);

        return form;
    }

    private MultiValueMap<String, String> buildScrobbleParams(
            LastFmSession session,
            PlaybackHistoryItem historyItem
    ) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.add("method", "track.scrobble");
        form.add("api_key", properties.apiKey());
        form.add("sk", session.sessionKey());
        form.add("artist", historyItem.artist());
        form.add("track", historyItem.title());
        form.add("timestamp", String.valueOf(historyItem.playedAt().getEpochSecond()));
        form.add("duration", String.valueOf(historyItem.duration()));

        if (historyItem.album() != null) {
            form.add("album", historyItem.album());
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
