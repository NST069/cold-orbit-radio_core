package com.coradio.rotation.domain.context;

import java.util.UUID;

public record RecentTrack(
        UUID trackId,
        String artist,
        String title,
        long playedAt
) {
}
