package com.coradio.rotation.infrastructure.out.scrobble.librefm;

import com.coradio.rotation.domain.enums.ScrobblerProvider;
import com.coradio.rotation.infrastructure.exception.ScrobblerApiException;
import com.coradio.rotation.infrastructure.exception.ScrobblerAuthenticationException;
import com.coradio.rotation.infrastructure.exception.ScrobblerBadSessionException;
import org.springframework.stereotype.Component;

@Component
public class LibreFmResponseParser {

    public void validateResponse(String response) {
        if (response == null)
            throw new ScrobblerApiException("[" + ScrobblerProvider.LIBREFM.name() + "] Empty response");

        String line = response.lines().findFirst().orElse("");

        if (line.startsWith("FAILED")) {
            throw new ScrobblerApiException("[" + ScrobblerProvider.LIBREFM.name() + "] Request failed: " + line);
        }

        switch (line) {
            case "OK" -> {
            }
            case "BADSESSION" ->
                    throw new ScrobblerBadSessionException("[" + ScrobblerProvider.LIBREFM.name() + "] Session Expired: " + line);
            case "BANNED" ->
                    throw new ScrobblerAuthenticationException("[" + ScrobblerProvider.LIBREFM.name() + "] " + line);
            default -> throw new ScrobblerApiException("[" + ScrobblerProvider.LIBREFM.name() + "] " + response);
        }
    }
}
