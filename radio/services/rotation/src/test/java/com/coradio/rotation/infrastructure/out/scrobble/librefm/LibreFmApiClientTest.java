package com.coradio.rotation.infrastructure.out.scrobble.librefm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import java.net.URI;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibreFmApiClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private LibreFmResponseParser responseParser;

    @Mock
    private RestClient.RequestBodyUriSpec request;

    @Mock
    private RestClient.RequestBodySpec bodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Test
    void shouldValidateResponse() {
        URI uri = URI.create("https://libre.fm/api");
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        String response = "OK";

        when(restClient.post()).thenReturn(request);
        when(request.uri(uri)).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .thenReturn(bodySpec);
        when(bodySpec.body(form)).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(response);

        new LibreFmApiClient(restClient, responseParser)
                .execute(uri, form);

        verify(responseParser).validateResponse(response);
    }
}
