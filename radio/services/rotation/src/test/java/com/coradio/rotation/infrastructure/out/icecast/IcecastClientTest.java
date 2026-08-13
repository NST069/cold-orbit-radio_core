package com.coradio.rotation.infrastructure.out.icecast;

import com.coradio.rotation.domain.context.StationInfo;
import com.coradio.rotation.infrastructure.exception.IcecastSourceNotFound;
import com.coradio.rotation.infrastructure.out.icecast.config.IcecastProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringJUnitConfig
class IcecastClientTest {

    private MockRestServiceServer server;

    private IcecastClient client;

    @BeforeEach
    void setUp() {

        RestClient.Builder builder = RestClient.builder();

        server = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder
                .baseUrl("https://icecast")
                .build();

        IcecastProperties properties = new IcecastProperties("https://localhost:8000", "/radio.mp3");

        client = new IcecastClient(restClient, properties);
    }

    @Test
    void shouldFetchStationInfo() {

        server.expect(requestTo("https://icecast/status-json.xsl"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "icestats": {
                            "source": [
                              {
                                "server_name":"Cold Orbit Radio",
                                "server_description":"Interplanetary broadcast station",
                                "genre":"Various",
                                "listenurl":"http://localhost:8000/radio.mp3",
                                "title":"KTRSS - ATLAS",
                                "listeners":5,
                                "listener_peak":12,
                                "stream_start_iso8601":"2026-07-23T20:27:12+0000"
                              }
                            ]
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        StationInfo info = client.fetchStationInfo();

        assertThat(info.name()).isEqualTo("Cold Orbit Radio");
        assertThat(info.description()).isEqualTo("Interplanetary broadcast station");
        assertThat(info.genre()).isEqualTo("Various");
        assertThat(info.url()).isEqualTo("http://localhost:8000/radio.mp3");
        assertThat(info.currentSong()).isEqualTo("KTRSS - ATLAS");
        assertThat(info.listeners()).isEqualTo(5);
        assertThat(info.peakListeners()).isEqualTo(12);
        assertThat(info.streamStarted())
                .isEqualTo(Instant.parse("2026-07-23T20:27:12Z"));

        server.verify();
    }

    @Test
    void shouldDecodeHtmlEntities() {

        server.expect(requestTo("https://icecast/status-json.xsl"))
                .andRespond(withSuccess("""
                        {
                          "icestats": {
                            "source": [
                              {
                                "server_name":"Cold Orbit Radio",
                                "server_description":"Station",
                                "genre":"Various",
                                "listenurl":"http://localhost:8000/radio.mp3",
                                "title":"&#12510;&#12463;&#12525;&#12473;MACROSS 82-99 - &#25126;&#22580;",
                                "listeners":0,
                                "listener_peak":0,
                                "stream_start_iso8601":"2026-07-23T20:27:12+0000"
                              }
                            ]
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        StationInfo info = client.fetchStationInfo();

        assertThat(info.currentSong())
                .isEqualTo("マクロスMACROSS 82-99 - 戦場");

        server.verify();
    }

    @Test
    void shouldSelectConfiguredMount() {

        server.expect(requestTo("https://icecast/status-json.xsl"))
                .andRespond(withSuccess("""
                        {
                          "icestats": {
                            "source": [
                              {
                                "server_name":"Test Station",
                                "listenurl":"http://localhost:8000/test.mp3",
                                "title":"Test",
                                "listeners":1,
                                "listener_peak":1,
                                "stream_start_iso8601":"2026-07-23T20:27:12+0000"
                              },
                              {
                                "server_name":"Cold Orbit Radio",
                                "server_description":"Interplanetary broadcast station",
                                "genre":"Various",
                                "listenurl":"http://localhost:8000/radio.mp3",
                                "title":"KTRSS - ATLAS",
                                "listeners":7,
                                "listener_peak":15,
                                "stream_start_iso8601":"2026-07-23T20:27:12+0000"
                              }
                            ]
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        StationInfo info = client.fetchStationInfo();

        assertThat(info.name()).isEqualTo("Cold Orbit Radio");
        assertThat(info.listeners()).isEqualTo(7);

        server.verify();
    }

    @Test
    void shouldThrowWhenMountNotFound() {

        server.expect(requestTo("https://icecast/status-json.xsl"))
                .andRespond(withSuccess("""
                        {
                          "icestats": {
                            "source": [
                              {
                                "server_name":"Another Station",
                                "listenurl":"http://localhost:8000/test.mp3",
                                "title":"Track"
                              }
                            ]
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchStationInfo())
                .isInstanceOf(IcecastSourceNotFound.class);

        server.verify();
    }
}
