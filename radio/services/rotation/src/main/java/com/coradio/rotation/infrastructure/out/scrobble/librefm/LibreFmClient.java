package com.coradio.rotation.infrastructure.out.scrobble.librefm;

import com.coradio.rotation.domain.enums.ScrobblerProvider;
import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.model.ScrobbleJobItem;
import com.coradio.rotation.infrastructure.exception.ScrobblerApiException;
import com.coradio.rotation.infrastructure.exception.ScrobblerAuthenticationException;
import com.coradio.rotation.infrastructure.exception.ScrobblerBadSessionException;
import com.coradio.rotation.infrastructure.out.scrobble.librefm.dto.LibreFmSession;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import java.net.URI;

@Component
public class LibreFmClient {

    private final RestClient restClient;
    private final LibreFmAuthenticator authenticator;

    public LibreFmClient(@Qualifier("LibreFm") RestClient restClient, LibreFmAuthenticator authenticator) {
        this.restClient = restClient;
        this.authenticator = authenticator;
    }

    public void updateNowPlaying(PlaybackHistoryItem historyItem) {
        LibreFmSession session = getOrAuthenticate();

        MultiValueMap<String, String> form = buildNowPlayingParams(session, historyItem);

        try {
            execute(session.nowPlayingUrl(), form);
        } catch (ScrobblerBadSessionException e) {
            authenticator.invalidate();

            session = authenticator.authenticate();
            form = buildNowPlayingParams(session, historyItem);

            execute(session.nowPlayingUrl(), form);
        }
    }

    public void scrobble(ScrobbleJobItem jobItem) {
        LibreFmSession session = getOrAuthenticate();

        PlaybackHistoryItem historyItem = jobItem.playbackHistoryItem();

        MultiValueMap<String, String> form = buildScrobbleParams(session, historyItem);

        try {
            execute(session.submissionUrl(), form);
        } catch (ScrobblerBadSessionException e) {
            authenticator.invalidate();

            session = authenticator.authenticate();
            form = buildScrobbleParams(session, historyItem);

            execute(session.submissionUrl(), form);
        }
    }

    private MultiValueMap<String, String> buildNowPlayingParams(LibreFmSession session, PlaybackHistoryItem historyItem) {

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.add("s", session.sessionKey());
        form.add("a", historyItem.artist());
        form.add("t", historyItem.title());
        form.add("l", String.valueOf(historyItem.duration()));
        form.add("n", "");
        form.add("m", "");

        if (historyItem.album() != null) form.add("b", historyItem.album());

        return form;
    }

    private MultiValueMap<String, String> buildScrobbleParams(LibreFmSession session, PlaybackHistoryItem historyItem) {

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.add("s", session.sessionKey());
        form.add("a[0]", historyItem.artist());
        form.add("t[0]", historyItem.title());
        form.add("i[0]", String.valueOf(historyItem.playedAt().getEpochSecond()));
        form.add("o[0]", "P");
        form.add("r[0]", "");
        form.add("l[0]", String.valueOf(historyItem.duration()));
        form.add("n[0]", "");
        form.add("m[0]", "");

        if (historyItem.album() != null) form.add("b[0]", historyItem.album());

        return form;
    }

    private void execute(URI uri, MultiValueMap<String, String> form) {

        String response = restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(String.class);

        validateResponse(response);
    }

    private void validateResponse(String response) {
        if (response == null)
            throw new ScrobblerApiException("[" + ScrobblerProvider.LIBREFM.name() + "] Empty response");

        String line = response.lines().findFirst().orElse("");

        if (line.startsWith("FAILED")) {
            throw new ScrobblerApiException("[" + ScrobblerProvider.LIBREFM.name() + "] Request failed: " + line);
        }

        switch (line) {
            case "OK" -> {
            }
            case "BADSESSION" ->
                    throw new ScrobblerBadSessionException("[" + ScrobblerProvider.LIBREFM.name() + "] Session Expired: " + line);
            case "BANNED" ->
                    throw new ScrobblerAuthenticationException("[" + ScrobblerProvider.LIBREFM.name() + "] " + line);
            default -> throw new ScrobblerApiException("[" + ScrobblerProvider.LIBREFM.name() + "] " + response);
        }
    }

    private LibreFmSession getOrAuthenticate() {

        LibreFmSession session = authenticator.currentSession();

        if (session == null) {
            session = authenticator.authenticate();
        }

        return session;
    }

}
