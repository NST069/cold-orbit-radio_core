package com.coradio.rotation.infrastructure.out.liquidsoap;

import com.coradio.rotation.domain.port.out.liquidsoap.PlaybackEnginePort;
import com.coradio.rotation.infrastructure.out.liquidsoap.config.LiquidsoapProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
@Slf4j
public class LiquidsoapTelnetAdapter implements PlaybackEnginePort {

    private final LiquidsoapClient client;

    private final LiquidsoapProperties properties;

    @Override
    public int getQueueLength() {
        return Integer.parseInt(
                client.execute("coldorbit.length").trim()
        );
    }

    @Override
    public Optional<String> getCurrentTrack() {
        return Optional.of(
                client.execute("coldorbit.current").trim()
        ).filter(Predicate.not(String::isBlank));
    }

    @Override
    public void enqueue(String localPath) {
        client.execute("coldorbit.push " + normalizePath(localPath));
    }

    private String normalizePath(String path) {
        return path.replace("\\", "/");
    }
}
