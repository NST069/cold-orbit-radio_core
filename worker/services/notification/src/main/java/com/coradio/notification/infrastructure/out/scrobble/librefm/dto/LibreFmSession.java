package com.coradio.notification.infrastructure.out.scrobble.librefm.dto;

import java.net.URI;

public record LibreFmSession(
        String sessionKey,
        URI nowPlayingUrl,
        URI submissionUrl
) {

}
