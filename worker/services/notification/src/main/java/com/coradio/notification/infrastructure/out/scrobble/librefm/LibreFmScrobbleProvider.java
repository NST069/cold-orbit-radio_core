package com.coradio.notification.infrastructure.out.scrobble.librefm;

import com.coradio.notification.domain.enums.ScrobblerProvider;
import com.coradio.notification.domain.model.ScrobbleTrack;
import com.coradio.notification.domain.out.scrobbler.ScrobbleProviderPort;
import com.coradio.notification.domain.enums.ScrobbleResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LibreFmScrobbleProvider implements ScrobbleProviderPort {

    private final LibreFmClient client;

    private final LibreFmProperties properties;

    @Override
    public ScrobblerProvider provider() {
        return ScrobblerProvider.LIBREFM;
    }

    @Override
    public boolean enabled() {
        return properties.enabled();
    }

    @Override
    public boolean supportsNowPlaying() {
        return properties.supportsNowPlaying();
    }

    @Override
    public ScrobbleResult scrobble(ScrobbleTrack scrobbleTrack, long playedAt) {
        return client.scrobble(scrobbleTrack, playedAt);
    }

    @Override
    public ScrobbleResult updateNowPlaying(ScrobbleTrack scrobbleTrack) {
        return client.updateNowPlaying(scrobbleTrack);
    }

}
