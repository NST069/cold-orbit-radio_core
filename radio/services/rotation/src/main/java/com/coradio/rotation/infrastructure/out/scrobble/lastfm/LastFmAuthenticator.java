package com.coradio.rotation.infrastructure.out.scrobble.lastfm;

import com.coradio.rotation.infrastructure.out.scrobble.lastfm.dto.LastFmSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Component
@Slf4j
public class LastFmAuthenticator {

    private final RestClient restClient;
    private final LastFmSessionHolder sessionHolder;
    private final LastFmProperties properties;
    private final LastFmResponseParser parser;
    private final LastFmApiSigner signer;

    public LastFmAuthenticator(@Qualifier("LastFm") RestClient restClient, LastFmSessionHolder sessionHolder, LastFmProperties properties, LastFmResponseParser parser, LastFmApiSigner signer) {
        this.restClient = restClient;
        this.sessionHolder = sessionHolder;
        this.properties = properties;
        this.parser = parser;
        this.signer = signer;
    }

    public String getToken() {
        Map<String, String> params = Map.of(
                "api_key", properties.apiKey(),
                "method", "auth.getToken"
        );

        String apiSig = signer.sign(
                params,
                properties.apiSecret()
        );

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("method", "auth.getToken")
                        .queryParam("api_key", properties.apiKey())
                        .queryParam("api_sig", apiSig)
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(String.class);

        log.debug("Lastfm Token: {}", response);

        return parser.parseToken(response);
    }

    public LastFmSession authenticate() {
        LastFmSession session;
        if (properties.sessionKey().isBlank()) {
            String token = getToken();

            Map<String, String> params = Map.of(
                    "api_key", properties.apiKey(),
                    "method", "auth.getSession",
                    "token", token
            );

            String apiSig = signer.sign(params, properties.apiSecret());

            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("method", "auth.getSession")
                            .queryParam("api_key", properties.apiKey())
                            .queryParam("token", token)
                            .queryParam("api_sig", apiSig)
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .body(String.class);

            log.debug("LastFm Auth response: {}", response);

            session = parser.parseSession(response);
        } else session = new LastFmSession(properties.sessionKey());

        sessionHolder.setSession(session);

        return session;
    }

    public LastFmSession currentSession() {
        return sessionHolder.getSession();
    }

    public void invalidate() {
        sessionHolder.setSession(null);
    }

}
