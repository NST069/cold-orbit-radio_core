package com.coradio.notification.application.service;

import com.coradio.notification.domain.enums.ScrobbleResult;
import com.coradio.notification.domain.model.ScrobbleTrack;
import com.coradio.notification.domain.out.scrobbler.ScrobbleProviderPort;
import com.coradio.notification.domain.out.scrobbler.ScrobbleProviderRegistryPort;
import com.coradio.notification.domain.port.in.ProcessNowPlayingEventUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessNowPlayingEventService implements ProcessNowPlayingEventUseCase {

    private final ScrobbleProviderRegistryPort registry;

    @Override
    public boolean update(UUID eventId, ScrobbleTrack track, long expiresAt) {

        if (expiresAt >= 0 && expiresAt < Instant.now().getEpochSecond()) {
            log.info("Event {} expired. Skipping", eventId);
            return true;
        }

        List<ScrobbleResult> results = registry.getProviders()
                .stream()
                .filter(ScrobbleProviderPort::supportsNowPlaying)
                .map(provider -> {
                    try {
                        log.debug("Updating nowPlaying for {} to {}", provider.provider(), track.artist() + " - " + track.title());

                        return provider.updateNowPlaying(track);
                    } catch (Exception ex) {
                        log.error("Error updating nowPlaying for {}", provider.provider(), ex);
                        return ScrobbleResult.FAILURE;
                    }
                }).toList();

        boolean completed = results.stream()
                .noneMatch(result -> result == ScrobbleResult.FAILURE);

        log.info("Status: [{}], completed: {}", results.stream().map(ScrobbleResult::name).collect(Collectors.joining(", ")), completed);

        return completed;
    }

}
