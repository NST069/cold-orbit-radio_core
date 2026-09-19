package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.context.TrackStatsStateContext;
import com.coradio.rotation.domain.port.in.TrackStatsUseCase;
import com.coradio.rotation.domain.port.out.persistence.TrackStatsRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackStatsService implements TrackStatsUseCase {

    private final TrackStatsStateContext state;
    private final TrackStatsRepositoryPort trackStatsRepository;

    public void flush() {
        trackStatsRepository.actualizeTrackStats(state);
    }
}
