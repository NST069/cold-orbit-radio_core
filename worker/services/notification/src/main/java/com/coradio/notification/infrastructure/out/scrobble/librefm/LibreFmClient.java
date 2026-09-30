package com.coradio.notification.infrastructure.out.scrobble.librefm;

import com.coradio.notification.domain.port.model.ScrobbleTrack;
import com.coradio.notification.infrastructure.exception.ScrobblerBadSessionException;
import com.coradio.notification.domain.port.enums.ScrobbleResult;
import com.coradio.notification.infrastructure.out.scrobble.librefm.dto.LibreFmSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
@RequiredArgsConstructor
public class LibreFmClient {

    private final LibreFmAuthenticator authenticator;
    private final LibreFmApiClient apiClient;

    public ScrobbleResult updateNowPlaying(ScrobbleTrack scrobbleTrack) {
        LibreFmSession session = getOrAuthenticate();

        MultiValueMap<String, String> form = buildNowPlayingParams(session, scrobbleTrack);

        try {
            return apiClient.execute(session.nowPlayingUrl(), form);
        } catch (ScrobblerBadSessionException e) {
            authenticator.invalidate();

            session = authenticator.authenticate();
            form = buildNowPlayingParams(session, scrobbleTrack);

            return apiClient.execute(session.nowPlayingUrl(), form);
        }
    }

    public ScrobbleResult scrobble(ScrobbleTrack scrobbleTrack, long playedAt) {
        LibreFmSession session = getOrAuthenticate();

        MultiValueMap<String, String> form = buildScrobbleParams(session, scrobbleTrack, playedAt);

        try {
            return apiClient.execute(session.submissionUrl(), form);
        } catch (ScrobblerBadSessionException e) {
            authenticator.invalidate();

            session = authenticator.authenticate();
            form = buildScrobbleParams(session, scrobbleTrack, playedAt);

            return apiClient.execute(session.submissionUrl(), form);
        }
    }

    private MultiValueMap<String, String> buildNowPlayingParams(
            LibreFmSession session,
            ScrobbleTrack scrobbleTrack
    ) {

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.add("s", session.sessionKey());
        form.add("a", scrobbleTrack.artist());
        form.add("t", scrobbleTrack.title());
        form.add("l", String.valueOf(scrobbleTrack.duration()));
        form.add("n", "");
        form.add("m", "");

        if (scrobbleTrack.album() != null) form.add("b", scrobbleTrack.album());

        return form;
    }

    private MultiValueMap<String, String> buildScrobbleParams(
            LibreFmSession session,
            ScrobbleTrack scrobbleTrack,
            long playedAt
    ) {

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.add("s", session.sessionKey());
        form.add("a[0]", scrobbleTrack.artist());
        form.add("t[0]", scrobbleTrack.title());
        form.add("i[0]", String.valueOf(playedAt));
        form.add("o[0]", "P");
        form.add("r[0]", "");
        form.add("l[0]", String.valueOf(scrobbleTrack.duration()));
        form.add("n[0]", "");
        form.add("m[0]", "");

        if (scrobbleTrack.album() != null) form.add("b[0]", scrobbleTrack.album());

        return form;
    }

    private LibreFmSession getOrAuthenticate() {

        LibreFmSession session = authenticator.currentSession();

        if (session == null) {
            session = authenticator.authenticate();
        }

        return session;
    }

}
