package com.coradio.rotation.domain.model;

import java.util.UUID;

public record ScrobbleTrack(
        UUID trackId,
        String artist,
        String title,
        String album,
        long duration
) {
}
