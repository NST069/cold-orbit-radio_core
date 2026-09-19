package com.coradio.rotation.domain.port.out.dj;

import com.coradio.rotation.application.dto.TrackInfo;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TrackSelectionStrategy {

    List<TrackInfo> selectTracks(
            List<TrackInfo> candidates,
            int count,
            Set<UUID> history
    );
}
