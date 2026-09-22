package com.coradio.rotation.domain.context;

import lombok.Getter;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
public class TrackStatsStateContext {

    private Map<UUID, TrackStatsDelta> stats = new ConcurrentHashMap<>();

    private long lastUpd;

    public void incrementPlayCount(UUID trackId) {
        stats.computeIfAbsent(
                trackId,
                id -> new TrackStatsDelta(0, 0)
        ).incrementPlayCount();
        lastUpd = Instant.now().toEpochMilli();
    }

    public TrackStatsDelta get(UUID trackId) {
        return stats.get(trackId);
    }

    public void replace(Map<UUID, TrackStatsDelta> restored) {
        stats.clear();
        stats.putAll(restored);
        lastUpd = Instant.now().toEpochMilli();
    }

    public Map<UUID, TrackStatsDelta> swap() {
        Map<UUID, TrackStatsDelta> current = stats;
        stats = new ConcurrentHashMap<>();
        lastUpd = Instant.now().toEpochMilli();
        return current;
    }

    public synchronized Map<UUID, TrackStatsDelta> drain() {
        Map<UUID, TrackStatsDelta> flushed = stats;
        stats = new ConcurrentHashMap<>();
        lastUpd = Instant.now().toEpochMilli();
        return flushed;
    }

    public void clear() {
        stats.clear();
        lastUpd = Instant.now().toEpochMilli();
    }

}
