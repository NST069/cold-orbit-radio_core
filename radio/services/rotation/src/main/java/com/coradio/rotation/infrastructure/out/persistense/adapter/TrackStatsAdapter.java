package com.coradio.rotation.infrastructure.out.persistense.adapter;

import com.coradio.rotation.domain.context.TrackStatsDelta;
import com.coradio.rotation.domain.context.TrackStatsStateContext;
import com.coradio.rotation.domain.port.out.persistence.TrackStatsRepositoryPort;
import com.coradio.rotation.infrastructure.out.persistense.repository.TrackStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class TrackStatsAdapter implements TrackStatsRepositoryPort {

    private final TrackStatsRepository trackStatsRepository;

    @Override
    @Transactional
    public void actualizeTrackStats(TrackStatsStateContext stateContext) {
        Map<UUID, TrackStatsDelta> stats = stateContext.drain();
        log.debug("Found {} tracks with updated stats", stats.size());
        if (stats.isEmpty()) return;
        stats.forEach((trackId, delta) ->
                trackStatsRepository.upsert(
                        trackId,
                        BigInteger.valueOf(delta.getPlays()),
                        BigInteger.valueOf(delta.getLikes())
                )
        );
    }
}
