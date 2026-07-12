package com.coradio.rotation.infrastructure.out.scrobble.librefm;

import com.coradio.rotation.domain.enums.ScrobblerProvider;
import com.coradio.rotation.infrastructure.exception.ScrobblerAuthenticationException;
import com.coradio.rotation.infrastructure.out.scrobble.librefm.dto.LibreFmSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestClient;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class LibreFmAuthenticator {

    private static final String CLIENT_ID = "cor";
    private static final String CLIENT_VERSION = "1.0";

    private final RestClient restClient;
    private final LibreFmSessionHolder sessionHolder;
    private final LibreFmProperties properties;
    private final LibreFmHandshakeParser parser;

    public LibreFmSession authenticate() {

        long timestamp = Instant.now().getEpochSecond();

        String token = buildAuthToken(
                properties.password(),
                timestamp
        );

        String response =
                restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .queryParam("hs", "true")
                                .queryParam("p", "1.2.1")
                                .queryParam("c", CLIENT_ID)
                                .queryParam("v", CLIENT_VERSION)
                                .queryParam("u", properties.username())
                                .queryParam("t", timestamp)
                                .queryParam("a", token)
                                .build())
                        .exchange((req, res) -> {
                            log.info("Status: {}", res.getStatusCode());

                            String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                            log.info("Body: {}", body);

                            return body;
                        });

        if (response == null || response.isBlank())
            throw new ScrobblerAuthenticationException("[" + ScrobblerProvider.LIBREFM.name() + "] Scrobbler returns empty response");

        LibreFmSession session = parser.parse(response);

        sessionHolder.setSession(session);

        return session;
    }

    private String buildAuthToken(String password, long timestamp) {

        String passwordHash = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));

        return DigestUtils.md5DigestAsHex(
                (passwordHash + timestamp)
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    public LibreFmSession currentSession() {
        return sessionHolder.getSession();
    }

    public void invalidate() {
        sessionHolder.setSession(null);
    }

}
