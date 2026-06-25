package com.coradio.rotation.domain.port.out.dj;

import com.coradio.rotation.application.dto.TrackInfo;
import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import java.util.List;

public interface TrackSelectionStrategy {

    List<TrackInfo> selectTracks(
            List<TrackInfo> candidates,
            int count,
            List<PlaybackHistoryItem> history
    );
}
