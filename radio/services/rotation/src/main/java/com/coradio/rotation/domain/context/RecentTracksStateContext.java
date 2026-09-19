package com.coradio.rotation.domain.context;

import com.coradio.rotation.application.config.QueueProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecentTracksStateContext {

    private final int HISTORY_TRACK_COUNT = 10;

    private final QueueProperties properties;

    private final Deque<RecentTrack> tracks = new ArrayDeque<>();

    private long lastUpd;

    public void add(UUID trackId, String artist, String title, Instant playedAt) {
        RecentTrack track = new RecentTrack(trackId, artist, title, playedAt.toEpochMilli());
        tracks.addFirst(track);
        removeExpired(playedAt);
        lastUpd = Instant.now().toEpochMilli();
    }

    public Deque<RecentTrack> getRecent() {
        return tracks;
    }

    public List<RecentTrack> getRecentHistory() {
        return tracks.stream()
                .limit(HISTORY_TRACK_COUNT)
                .toList();
    }

    public Set<UUID> getRecentTrackIds(int candidatesSize) {
        return tracks.stream()
                .map(RecentTrack::trackId)
                .limit(candidatesSize / 2)
                .collect(Collectors.toSet());
    }

    private void removeExpired(Instant now) {
        long threshold = now.minus(properties.historyHours(), ChronoUnit.HOURS).toEpochMilli();

        while (tracks.size() > HISTORY_TRACK_COUNT && tracks.getLast().playedAt() < threshold) {
            tracks.removeLast();
            lastUpd = Instant.now().toEpochMilli();
        }
    }

    public void replace(Deque<RecentTrack> restored) {
        tracks.clear();
        tracks.addAll(restored);
        lastUpd = Instant.now().toEpochMilli();
    }

}
