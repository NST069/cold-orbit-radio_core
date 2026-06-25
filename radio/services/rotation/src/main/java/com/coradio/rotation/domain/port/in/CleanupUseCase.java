package com.coradio.rotation.domain.port.in;

import java.time.Duration;

public interface CleanupUseCase {

    void cleanupProcessedTracks(Duration retention);

    void cleanupOrphanFiles(Duration retention);
}
