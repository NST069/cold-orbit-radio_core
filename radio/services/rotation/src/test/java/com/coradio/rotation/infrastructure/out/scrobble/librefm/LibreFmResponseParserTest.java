package com.coradio.rotation.infrastructure.out.scrobble.librefm;

import com.coradio.rotation.infrastructure.exception.ScrobblerApiException;
import com.coradio.rotation.infrastructure.exception.ScrobblerAuthenticationException;
import com.coradio.rotation.infrastructure.exception.ScrobblerBadSessionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LibreFmResponseParserTest {

    private final LibreFmResponseParser parser = new LibreFmResponseParser();

    @Test
    void shouldAcceptOkResponse() {
        assertDoesNotThrow(() ->
                parser.validateResponse("OK")
        );
    }

    @Test
    void shouldThrowExceptionForEmptyResponse() {
        ScrobblerApiException exception = assertThrows(
                ScrobblerApiException.class,
                () -> parser.validateResponse(null)
        );

        assertEquals(
                "[LIBREFM] Empty response",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionForFailedResponse() {
        ScrobblerApiException exception = assertThrows(
                ScrobblerApiException.class,
                () -> parser.validateResponse("FAILED Something went wrong")
        );

        assertEquals(
                "[LIBREFM] Request failed: FAILED Something went wrong",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowBadSessionException() {
        ScrobblerBadSessionException exception = assertThrows(
                ScrobblerBadSessionException.class,
                () -> parser.validateResponse("BADSESSION")
        );

        assertEquals(
                "[LIBREFM] Session Expired: BADSESSION",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowAuthenticationException() {
        ScrobblerAuthenticationException exception = assertThrows(
                ScrobblerAuthenticationException.class,
                () -> parser.validateResponse("BANNED")
        );

        assertEquals(
                "[LIBREFM] BANNED",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionForUnknownResponse() {
        ScrobblerApiException exception = assertThrows(
                ScrobblerApiException.class,
                () -> parser.validateResponse("BADTIME")
        );

        assertEquals(
                "[LIBREFM] BADTIME",
                exception.getMessage()
        );
    }

    @Test
    void shouldValidateOnlyFirstLine() {
        assertDoesNotThrow(() ->
                parser.validateResponse("OK\nsome session data")
        );
    }

}
