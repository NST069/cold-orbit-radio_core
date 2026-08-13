package com.coradio.rotation.application.dto.response;

import java.util.UUID;

public record NowPlayingResponse(
        UUID id,
        String title,
        String performer,
        int duration,
        boolean hasCover
) {
}
