package com.coradio.rotation.infrastructure.out.scrobble.lastfm;

import com.coradio.rotation.infrastructure.exception.ScrobblerApiException;
import com.coradio.rotation.infrastructure.exception.ScrobblerAuthenticationException;
import com.coradio.rotation.infrastructure.exception.ScrobblerBadSessionException;
import com.coradio.rotation.infrastructure.out.scrobble.lastfm.dto.LastFmSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LastFmResponseParserTest {

    private LastFmResponseParser parser;

    @BeforeEach
    void setUp() {
        parser = new LastFmResponseParser();
    }

    @Test
    void parseSession_shouldReturnSession() {
        String response = """
                {
                  "session": {
                    "name": "test-user",
                    "key": "session-key",
                    "subscriber": 0
                  }
                }
                """;

        LastFmSession result = parser.parseSession(response);

        assertThat(result.sessionKey())
                .isEqualTo("session-key");
    }

    @Test
    void parseSession_shouldThrowWhenResponseIsNull() {
        assertThatThrownBy(() -> parser.parseSession(null))
                .isInstanceOf(ScrobblerAuthenticationException.class)
                .hasMessage("[LASTFM] Empty response");
    }

    @Test
    void parseSession_shouldThrowWhenResponseIsBlank() {
        assertThatThrownBy(() -> parser.parseSession("   "))
                .isInstanceOf(ScrobblerAuthenticationException.class)
                .hasMessage("[LASTFM] Empty response");
    }

    @Test
    void parseSession_shouldThrowWhenLastFmReturnsError() {
        String response = """
                {
                  "error": 14,
                  "message": "No method with that name exists"
                }
                """;

        assertThatThrownBy(() -> parser.parseSession(response))
                .isInstanceOf(ScrobblerAuthenticationException.class)
                .hasMessage("[LASTFM] No method with that name exists");
    }

    @Test
    void parseSession_shouldThrowWhenSessionKeyIsMissing() {
        String response = """
                {
                  "session": {
                    "name": "test-user"
                  }
                }
                """;

        assertThatThrownBy(() -> parser.parseSession(response))
                .isInstanceOf(ScrobblerAuthenticationException.class)
                .hasMessage("[LASTFM] Invalid authentication response");
    }

    @Test
    void parseSession_shouldThrowWhenSessionKeyIsBlank() {
        String response = """
                {
                  "session": {
                    "key": "   "
                  }
                }
                """;

        assertThatThrownBy(() -> parser.parseSession(response))
                .isInstanceOf(ScrobblerAuthenticationException.class)
                .hasMessage("[LASTFM] Invalid authentication response");
    }

    @Test
    void parseSession_shouldThrowWhenSessionObjectIsMissing() {
        String response = """
                {
                  "foo": "bar"
                }
                """;

        assertThatThrownBy(() -> parser.parseSession(response))
                .isInstanceOf(ScrobblerAuthenticationException.class)
                .hasMessage("[LASTFM] Invalid authentication response");
    }

    @Test
    void parseSession_shouldThrowWhenJsonIsInvalid() {
        String response = """
                {"session":
                """;

        assertThatThrownBy(() -> parser.parseSession(response))
                .isInstanceOf(ScrobblerAuthenticationException.class)
                .hasMessageStartingWith("[LASTFM] Invalid response:");
    }

    @Test
    void parseToken_shouldReturnToken() {
        String response = """
                {
                  "token": "abc123"
                }
                """;

        String result = parser.parseToken(response);

        assertThat(result)
                .isEqualTo("abc123");
    }

    @Test
    void parseToken_shouldReturnEmptyStringWhenTokenIsMissing() {
        String response = """
                {
                  "foo": "bar"
                }
                """;

        String result = parser.parseToken(response);

        assertThat(result)
                .isEmpty();
    }

    @Test
    void parseToken_shouldThrowWhenLastFmReturnsError() {
        String response = """
                {
                  "error": 14,
                  "message": "Invalid method"
                }
                """;

        assertThatThrownBy(() -> parser.parseToken(response))
                .isInstanceOf(ScrobblerAuthenticationException.class)
                .hasMessage("[LASTFM] Invalid method");
    }

    @Test
    void parseToken_shouldThrowWhenJsonIsInvalid() {
        String response = """
                {"token":
                """;

        assertThatThrownBy(() -> parser.parseToken(response))
                .isInstanceOf(ScrobblerAuthenticationException.class)
                .hasMessageStartingWith("[LASTFM] Invalid response:");
    }

    @Test
    void validateResponse_shouldAcceptSuccessfulResponse() {
        String response = """
                {
                  "scrobbles": {
                    "scrobble": []
                  }
                }
                """;

        parser.validateResponse(response);
    }

    @Test
    void validateResponse_shouldAcceptEmptyJsonObject() {
        parser.validateResponse("{}");
    }

    @Test
    void validateResponse_shouldThrowWhenResponseIsNull() {
        assertThatThrownBy(() -> parser.validateResponse(null))
                .isInstanceOf(ScrobblerApiException.class)
                .hasMessage("[LASTFM] Empty response");
    }

    @Test
    void validateResponse_shouldThrowWhenResponseIsBlank() {
        assertThatThrownBy(() -> parser.validateResponse("   "))
                .isInstanceOf(ScrobblerApiException.class)
                .hasMessage("[LASTFM] Empty response");
    }

    @Test
    void validateResponse_shouldThrowBadSessionExceptionForError9() {
        String response = """
                {
                  "error": 9,
                  "message": "Invalid session key"
                }
                """;

        assertThatThrownBy(() -> parser.validateResponse(response))
                .isInstanceOf(ScrobblerBadSessionException.class)
                .hasMessage("[LASTFM] Invalid session key");
    }

    @Test
    void validateResponse_shouldThrowApiExceptionForOtherErrors() {
        String response = """
                {
                  "error": 11,
                  "message": "Service Offline"
                }
                """;

        assertThatThrownBy(() -> parser.validateResponse(response))
                .isInstanceOf(ScrobblerApiException.class)
                .hasMessage("[LASTFM] Service Offline");
    }

    @Test
    void validateResponse_shouldThrowApiExceptionForErrorWithoutMessage() {
        String response = """
                {
                  "error": 11
                }
                """;

        assertThatThrownBy(() -> parser.validateResponse(response))
                .isInstanceOf(ScrobblerApiException.class)
                .hasMessage("[LASTFM] ");
    }

    @Test
    void validateResponse_shouldThrowWhenJsonIsInvalid() {
        String response = """
                {"error":
                """;

        assertThatThrownBy(() -> parser.validateResponse(response))
                .isInstanceOf(ScrobblerApiException.class)
                .hasMessageStartingWith("[LASTFM] Invalid response:");
    }

}
