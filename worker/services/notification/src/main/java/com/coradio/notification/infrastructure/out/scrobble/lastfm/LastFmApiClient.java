package com.coradio.notification.infrastructure.out.scrobble.lastfm;

import com.coradio.notification.domain.enums.ScrobbleResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.net.URI;

@Component
public class LastFmApiClient {

    private final LastFmResponseParser responseParser;
    private final RestClient restClient;

    public LastFmApiClient(@Qualifier("LastFm") RestClient restClient, LastFmResponseParser responseParser) {
        this.restClient = restClient;
        this.responseParser = responseParser;
    }

    public ScrobbleResult execute(URI uri, MultiValueMap<String, String> form) {
        try {
            String response = restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(String.class);

            return responseParser.validateResponse(response);

        } catch (ResourceAccessException e) {
            if (isTimeout(e)) {
                return ScrobbleResult.TIMEOUT;
            }

            return ScrobbleResult.FAILURE;
        }
    }

    private boolean isTimeout(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof SocketTimeoutException) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}
