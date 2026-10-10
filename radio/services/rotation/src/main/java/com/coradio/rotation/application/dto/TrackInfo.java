package com.coradio.rotation.application.dto;

import java.util.UUID;

public record TrackInfo(
        UUID id,
        String artist,
        String title,
        String album,
        int duration,
        String storageKey
) {
}
