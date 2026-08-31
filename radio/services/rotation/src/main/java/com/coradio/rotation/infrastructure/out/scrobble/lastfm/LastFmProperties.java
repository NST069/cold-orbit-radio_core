package com.coradio.rotation.infrastructure.out.scrobble.lastfm;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("scrobbler.lastfm")
public record LastFmProperties(
        String apiUrl,
        boolean enabled,
        boolean supportsNowPlaying,
        String apiKey,
        String apiSecret,
        String sessionKey
) {

}
