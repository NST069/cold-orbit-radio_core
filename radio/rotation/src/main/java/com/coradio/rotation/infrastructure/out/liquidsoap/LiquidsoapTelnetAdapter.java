package com.coradio.rotation.infrastructure.out.liquidsoap;

import com.coradio.rotation.domain.port.out.liquidsoap.PlaybackEnginePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LiquidsoapTelnetAdapter implements PlaybackEnginePort {
    @Override
    public int getQueueLength() {
        return 0;
    }

    @Override
    public Optional<String> getCurrentTrack() {
        return Optional.empty();
    }

    @Override
    public void enqueue(String localPath) {

    }
}
