package com.coradio.notification.domain.out.scrobbler;

import com.coradio.notification.domain.enums.ScrobbleResult;
import com.coradio.notification.domain.enums.ScrobblerProvider;
import com.coradio.notification.domain.model.ScrobbleTrack;

public interface ScrobbleProviderPort {

    ScrobblerProvider provider();

    boolean enabled();

    boolean supportsNowPlaying();

    ScrobbleResult scrobble(ScrobbleTrack scrobbleTrack, long playedAt);

    ScrobbleResult updateNowPlaying(ScrobbleTrack scrobbleTrack);

}
