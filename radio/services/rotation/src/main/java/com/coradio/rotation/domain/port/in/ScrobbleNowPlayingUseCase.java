package com.coradio.rotation.domain.port.in;

import com.coradio.rotation.domain.model.PlaybackHistoryItem;

public interface ScrobbleNowPlayingUseCase {

    void update(PlaybackHistoryItem historyItem);
}
