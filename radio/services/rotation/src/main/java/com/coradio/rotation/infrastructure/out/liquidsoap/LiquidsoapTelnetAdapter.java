package com.coradio.rotation.infrastructure.out.liquidsoap;

import com.coradio.rotation.domain.port.out.liquidsoap.PlaybackEnginePort;
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
    public Optional<String> getCurrentTrackDuration() {
        return Optional.of(
                client.execute("coldorbit.duration").trim()
        ).filter(Predicate.not(String::isBlank));
    }

    @Override
    public void enqueue(String localPath) {
        client.execute("coldorbit.push " + normalizePath(localPath));
    }

    @Override
    public boolean isAvailable() {
        try {
            client.execute("version");
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private String normalizePath(String path) {
        return path.replace("\\", "/");
    }

}
