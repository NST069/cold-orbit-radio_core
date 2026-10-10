package com.coradio.rotation.infrastructure.out.scrobble.lastfm;

import com.coradio.rotation.domain.enums.ScrobblerProvider;
import com.coradio.rotation.infrastructure.exception.ScrobblerApiException;
import com.coradio.rotation.infrastructure.exception.ScrobblerAuthenticationException;
import com.coradio.rotation.infrastructure.exception.ScrobblerBadSessionException;
import com.coradio.rotation.infrastructure.out.scrobble.lastfm.dto.LastFmSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class LastFmResponseParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LastFmSession parseSession(String response) {
        if (response == null || response.isBlank()) {
            throw new ScrobblerAuthenticationException("[" + ScrobblerProvider.LASTFM.name() + "] Empty response");
        }

        try {
            JsonNode root = objectMapper.readTree(response);

            if (root.has("error")) {
                throw new ScrobblerAuthenticationException("[" + ScrobblerProvider.LASTFM.name() + "] " + root.path("message").asText());
            }

            JsonNode session = root.path("session");
            String sessionKey = session.path("key").asText(null);

            if (sessionKey == null || sessionKey.isBlank()) {
                throw new ScrobblerAuthenticationException("[" + ScrobblerProvider.LASTFM.name() + "] Invalid authentication response");
            }

            return new LastFmSession(sessionKey);

        } catch (JsonProcessingException e) {
            throw new ScrobblerAuthenticationException("[" + ScrobblerProvider.LASTFM.name() + "] Invalid response: " + e.getMessage());
        }
    }

    public String parseToken(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);

            if (root.has("error")) {
                throw new ScrobblerAuthenticationException("[" + ScrobblerProvider.LASTFM.name() + "] " + root.path("message").asText());
            }

            return root.path("token").asText();

        } catch (JsonProcessingException e) {
            throw new ScrobblerAuthenticationException("[" + ScrobblerProvider.LASTFM.name() + "] Invalid response: " + e.getMessage());
        }
    }

    public void validateResponse(String response) {
        if (response == null || response.isBlank()) {
            throw new ScrobblerApiException("[" + ScrobblerProvider.LASTFM.name() + "] Empty response");
        }

        try {
            JsonNode root = objectMapper.readTree(response);

            if (root.has("error")) {
                int error = root.path("error").asInt();
                String message = root.path("message").asText();

                if (error == 9) {
                    throw new ScrobblerBadSessionException("[" + ScrobblerProvider.LASTFM.name() + "] " + message);
                }

                throw new ScrobblerApiException("[" + ScrobblerProvider.LASTFM.name() + "] " + message);
            }

        } catch (JsonProcessingException e) {
            throw new ScrobblerApiException("[" + ScrobblerProvider.LASTFM.name() + "] Invalid response: " + e.getMessage());
        }
    }

}
