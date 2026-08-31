package com.coradio.rotation.infrastructure.out.scrobble.lastfm;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URI;

@Component
public class LastFmApiClient {

    private final LastFmResponseParser responseParser;
    private final RestClient restClient;

    public LastFmApiClient(@Qualifier("LastFm") RestClient restClient, LastFmResponseParser responseParser) {
        this.restClient = restClient;
        this.responseParser = responseParser;
    }

    public void execute(URI uri, MultiValueMap<String, String> form) {

        String response = restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(String.class);

        responseParser.validateResponse(response);
    }
}
