package com.coradio.rotation.domain.port.out.scrobbler;

import com.coradio.rotation.domain.enums.ScrobblerProvider;
import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.model.ScrobbleJobItem;

public interface ScrobbleProviderPort {

    ScrobblerProvider provider();

    boolean enabled();

    boolean supportsNowPlaying();

    void scrobble(ScrobbleJobItem scrobbleJobItem);

    void updateNowPlaying(PlaybackHistoryItem historyItem);

}
