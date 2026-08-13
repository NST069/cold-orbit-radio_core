package com.coradio.rotation.infrastructure.out.scrobble.librefm;

import com.coradio.rotation.infrastructure.out.scrobble.librefm.dto.LibreFmSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class LibreFmAuthenticatorTest {

    private MockRestServiceServer server;

    private final LibreFmSessionHolder sessionHolder = new LibreFmSessionHolder();

    private final LibreFmHandshakeParser parser = new LibreFmHandshakeParser();

    private LibreFmAuthenticator authenticator;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();

        server = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder
                .baseUrl("https://libre.fm")
                .build();

        LibreFmProperties properties = new LibreFmProperties(
                "https://libre.fm",
                true,
                true,
                "user",
                "password"
        );

        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-07-12T12:00:00Z"),
                ZoneOffset.UTC
        );

        authenticator = new LibreFmAuthenticator(
                fixedClock,
                restClient,
                sessionHolder,
                properties,
                parser
        );
    }

    @Test
    void shouldAuthenticate() {

        server.expect(requestTo(startsWith("https://libre.fm?")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        OK
                        session123
                        https://libre.fm/1.x/nowplaying/1.2/
                        https://libre.fm/1.x/submissions/1.2/
                        """, MediaType.TEXT_PLAIN));

        LibreFmSession session = authenticator.authenticate();

        assertThat(session.sessionKey()).isEqualTo("session123");
        assertThat(sessionHolder.getSession()).isEqualTo(session);

        server.verify();
    }

}
