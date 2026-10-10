package com.coradio.notification.infrastructure.out.scrobble.lastfm;

import com.coradio.notification.infrastructure.out.scrobble.lastfm.dto.LastFmSession;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class LastFmSessionHolder {

    private LastFmSession session;
}
