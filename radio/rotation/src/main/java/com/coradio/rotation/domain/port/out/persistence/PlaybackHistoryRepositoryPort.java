package com.coradio.rotation.domain.port.out.persistence;

import com.coradio.rotation.domain.model.PlaybackHistoryItem;

public interface PlaybackHistoryRepositoryPort {
    PlaybackHistoryItem save(PlaybackHistoryItem playbackHistoryItem);
}
