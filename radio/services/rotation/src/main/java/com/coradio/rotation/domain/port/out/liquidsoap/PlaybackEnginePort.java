package com.coradio.rotation.domain.port.out.liquidsoap;

import java.util.Optional;

public interface PlaybackEnginePort {

    int getQueueLength();

    Optional<String> getCurrentTrack();

    void enqueue(String localPath);

}
