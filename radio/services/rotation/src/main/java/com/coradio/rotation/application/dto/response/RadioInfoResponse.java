package com.coradio.rotation.application.dto.response;

public record RadioInfoResponse(
        String name,
        String description,
        String genre,
        String url,
        int listeners,
        int peakListeners,
        String currentSong,
        String streamStart
) {
}
