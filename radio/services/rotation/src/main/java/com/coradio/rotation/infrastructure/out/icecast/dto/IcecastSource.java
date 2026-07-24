package com.coradio.rotation.infrastructure.out.icecast.dto;

import java.time.OffsetDateTime;

public record IcecastSource(

        String server_name,
        String server_description,

        String genre,

        String listenurl,

        String title,

        Integer listeners,
        Integer listener_peak,

        Integer bitrate,
        Integer samplerate,
        Integer channels,

        String server_type,

        OffsetDateTime stream_start_iso8601

) {
}
