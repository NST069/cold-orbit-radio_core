package com.coradio.notification.infrastructure.out.scrobble.librefm;

import com.coradio.notification.domain.enums.ScrobbleResult;
import com.coradio.notification.domain.enums.ScrobblerProvider;
import com.coradio.notification.infrastructure.exception.ScrobblerApiException;
import com.coradio.notification.infrastructure.exception.ScrobblerAuthenticationException;
import com.coradio.notification.infrastructure.exception.ScrobblerBadSessionException;
import org.springframework.stereotype.Component;

@Component
public class LibreFmResponseParser {

    public ScrobbleResult validateResponse(String response) {
        if (response == null)
            throw new ScrobblerApiException("[" + ScrobblerProvider.LIBREFM.name() + "] Empty response");

        String line = response.lines().findFirst().orElse("");

        if (line.startsWith("FAILED")) {
            throw new ScrobblerApiException("[" + ScrobblerProvider.LIBREFM.name() + "] Request failed: " + line);
        }

        switch (line) {
            case "OK" -> {
                return ScrobbleResult.SUCCESS;
            }
            case "BADSESSION" ->
                    throw new ScrobblerBadSessionException("[" + ScrobblerProvider.LIBREFM.name() + "] Session Expired: " + line);
            case "BANNED" ->
                    throw new ScrobblerAuthenticationException("[" + ScrobblerProvider.LIBREFM.name() + "] " + line);
            default -> throw new ScrobblerApiException("[" + ScrobblerProvider.LIBREFM.name() + "] " + response);
        }
    }
}
