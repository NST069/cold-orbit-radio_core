package com.coradio.rotation.infrastructure.out.scrobble.librefm;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("scrobbler.librefm")
public record LibreFmProperties(
        String apiUrl,
        boolean enabled,
        boolean supportsNowPlaying,
        String username,
        String password
) {

}
