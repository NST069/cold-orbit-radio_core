package com.coradio.rotation.domain.port.out.persistence;

import com.coradio.rotation.domain.context.TrackStatsStateContext;

public interface TrackStatsRepositoryPort {
    void actualizeTrackStats(TrackStatsStateContext stateContext);
}
