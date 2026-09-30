package com.coradio.notification.infrastructure.out.scrobble.librefm;

import com.coradio.notification.domain.port.enums.ScrobblerProvider;
import com.coradio.notification.infrastructure.exception.ScrobblerAuthenticationException;
import com.coradio.notification.infrastructure.out.scrobble.librefm.dto.LibreFmSession;
import org.springframework.stereotype.Component;
import java.net.URI;

@Component
public class LibreFmHandshakeParser {

    public LibreFmSession parse(String body) {

        String[] lines = body.split("\\R");

        if (!"OK".equals(lines[0])) {
            throw new ScrobblerAuthenticationException("[" + ScrobblerProvider.LIBREFM.name() + "] " + lines[0]);
        }

        return new LibreFmSession(
                lines[1],
                URI.create(lines[2]),
                URI.create(lines[3])
        );
    }

}
