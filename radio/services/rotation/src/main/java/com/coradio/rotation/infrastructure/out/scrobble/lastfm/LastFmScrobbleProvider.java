package com.coradio.rotation.infrastructure.out.scrobble.lastfm;

import com.coradio.rotation.domain.enums.ScrobblerProvider;
import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.model.ScrobbleJobItem;
import com.coradio.rotation.domain.port.out.scrobbler.ScrobbleProviderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LastFmScrobbleProvider implements ScrobbleProviderPort {

    private final LastFmClient client;

    private final LastFmProperties properties;

    @Override
    public ScrobblerProvider provider() {
        return ScrobblerProvider.LASTFM;
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
    public void scrobble(ScrobbleJobItem scrobbleJobItem) {
        client.scrobble(scrobbleJobItem);
    }

    @Override
    public void updateNowPlaying(PlaybackHistoryItem historyItem) {
        client.updateNowPlaying(historyItem);
    }

}
