package com.coradio.rotation.domain.port.in;

import com.coradio.rotation.domain.model.PlaybackHistoryItem;

public interface NowPlayingUseCase {

    void update(PlaybackHistoryItem historyItem);
}
