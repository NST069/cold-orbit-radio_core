package com.coradio.rotation.infrastructure.out.icecast.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;

public record IcecastStats(

        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        List<IcecastSource> source

) {
}
