package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.context.RecentTrack;
import com.coradio.rotation.domain.context.RecentTracksStateContext;
import com.coradio.rotation.domain.context.TrackStatsDelta;
import com.coradio.rotation.domain.context.TrackStatsStateContext;
import com.coradio.rotation.domain.port.in.RotationStateUseCase;
import com.coradio.rotation.domain.port.out.redis.RedisCachePort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RotationStateService implements RotationStateUseCase {

    private static final String STATS_KEY = "rotation:state:stats";
    private static final String HISTORY_KEY = "rotation:state:history";

    private final TrackStatsStateContext trackStats;
    private final RecentTracksStateContext recentTracks;

    private final RedisCachePort redisCache;

    private final ObjectMapper objectMapper = new ObjectMapper();


    public void save() {
        saveStats();
        saveHistory();
    }

    private void saveStats() {
        try {
            String json = objectMapper.writeValueAsString(trackStats.getStats());

            redisCache.put(
                    STATS_KEY,
                    json
            );
            log.debug("TrackStats saved");
        } catch (JsonProcessingException e) {
            log.error("Saving trackStats failed", e);
        }
    }

    private void saveHistory() {
        try {
            String json = objectMapper.writeValueAsString(recentTracks.getRecent());

            redisCache.put(
                    HISTORY_KEY,
                    json
            );
            log.debug("RecentTracks saved");
        } catch (JsonProcessingException e) {
            log.error("Saving recentTracks failed", e);
        }
    }

    public void restore() {
        redisCache.get(STATS_KEY).ifPresent(this::restoreStats);
        redisCache.get(HISTORY_KEY).ifPresent(this::restoreHistory);
    }

    private void restoreStats(String json) {
        try {
            Map<UUID, TrackStatsDelta> stats =
                    objectMapper.readValue(
                            json,
                            new TypeReference<>() {
                            }
                    );

            trackStats.replace(stats);
            log.debug("TrackStats restored");
        } catch (JsonProcessingException e) {
            log.error("Restoring trackStats failed", e);
        }
    }

    private void restoreHistory(String json) {
        try {
            Deque<RecentTrack> history =
                    objectMapper.readValue(
                            json,
                            new TypeReference<>() {
                            }
                    );

            recentTracks.replace(history);
            log.debug("RecentTracks restored");
        } catch (JsonProcessingException e) {
            log.error("Restoring recentTracks failed", e);
        }
    }

}
