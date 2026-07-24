package com.coradio.rotation.infrastructure.out.icecast;

import com.coradio.rotation.domain.context.StationInfo;
import com.coradio.rotation.domain.port.out.icecast.IcecastClientPort;
import com.coradio.rotation.infrastructure.exception.IcecastSourceNotFound;
import com.coradio.rotation.infrastructure.out.icecast.config.IcecastProperties;
import com.coradio.rotation.infrastructure.out.icecast.dto.IcecastResponse;
import com.coradio.rotation.infrastructure.out.icecast.dto.IcecastSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class IcecastClient implements IcecastClientPort {

    private final RestClient restClient;

    private final IcecastProperties properties;

    public IcecastClient(@Qualifier("Icecast") RestClient restClient, IcecastProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public StationInfo fetchStationInfo() {
        IcecastResponse response = restClient.get()
                .uri("/status-json.xsl")
                .retrieve()
                .body(IcecastResponse.class);
        assert response != null;
        IcecastSource source = response.icestats()
                .source()
                .stream()
                .filter(s -> s.listenurl().endsWith(properties.mountPoint()))
                .findFirst()
                .orElseThrow(() -> new IcecastSourceNotFound(properties.mountPoint()));
        return map(source);
    }

    private StationInfo map(IcecastSource source) {

        return new StationInfo(
                source.server_name(),
                source.server_description(),
                source.genre(),
                source.listenurl(),
                StringEscapeUtils.unescapeHtml4(source.title()),
                source.listeners(),
                source.listener_peak(),
                source.stream_start_iso8601().toInstant()
        );
    }

}
