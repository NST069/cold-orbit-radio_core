package com.coradio.rotation.application.dto.request;

public record LiquidsoapRequest(
        String event,
        Long timestamp,
        String artist,
        String title,
        String album,
        String duration,
        String genre,
        String uri
) {
}
