package com.coradio.rotation.infrastructure.out.scrobble.lastfm;

import org.junit.jupiter.api.Test;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LastFmApiSignerTest {

    private final LastFmApiSigner signer = new LastFmApiSigner();

    @Test
    void shouldBuildApiSignature() {
        Map<String, String> params = Map.of(
                "token", "abc",
                "method", "auth.getSession",
                "api_key", "key"
        );

        String result = signer.sign(params, "secret");

        assertEquals("6629efc98b97f7c35ff32314185ffaa1", result);
    }

    @Test
    void shouldSortParametersBeforeSigning() {
        Map<String, String> params1 = new LinkedHashMap<>();
        params1.put("token", "test-token");
        params1.put("method", "auth.getSession");
        params1.put("api_key", "test-key");

        Map<String, String> params2 = new LinkedHashMap<>();
        params2.put("api_key", "test-key");
        params2.put("method", "auth.getSession");
        params2.put("token", "test-token");

        assertEquals(signer.sign(params1, "test-secret"), signer.sign(params2, "test-secret"));
    }
}
