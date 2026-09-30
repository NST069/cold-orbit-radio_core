package com.coradio.notification.domain.port.out.scrobbler;

import com.coradio.notification.domain.port.enums.ScrobbleResult;
import com.coradio.notification.domain.port.enums.ScrobblerProvider;
import com.coradio.notification.domain.port.model.ScrobbleTrack;

public interface ScrobbleProviderPort {

    ScrobblerProvider provider();

    boolean enabled();

    boolean supportsNowPlaying();

    ScrobbleResult scrobble(ScrobbleTrack scrobbleTrack, long playedAt);

    ScrobbleResult updateNowPlaying(ScrobbleTrack scrobbleTrack);

}
