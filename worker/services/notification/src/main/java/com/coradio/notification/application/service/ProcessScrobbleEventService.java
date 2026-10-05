package com.coradio.notification.application.service;

import com.coradio.notification.domain.port.enums.ScrobbleResult;
import com.coradio.notification.domain.port.model.ScrobbleTrack;
import com.coradio.notification.domain.port.out.scrobbler.ScrobbleProviderRegistryPort;
import com.coradio.notification.infrastructure.in.ProcessScrobbleEventUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessScrobbleEventService implements ProcessScrobbleEventUseCase {

    private final ScrobbleProviderRegistryPort registry;

    @Override
    public boolean scrobble(UUID eventId, ScrobbleTrack track, long playedAt) {

        List<ScrobbleResult> results = registry.getProviders()
                .stream()
                .map(provider -> {
                    try {
                        log.debug("Scrobbling {} by {}", track.artist() + " - " + track.title(), provider.provider());

                        ScrobbleResult result = provider.scrobble(track, playedAt);
                        if (result.equals(ScrobbleResult.TIMEOUT))
                            log.warn("Timeout scrobbling event {} by {}", eventId, provider.provider());
                        return result;
                    } catch (Exception ex) {
                        log.error("Error scrobbling event {} by {}", eventId, provider.provider(), ex);
                        return ScrobbleResult.FAILURE;
                    }
                }).toList();

        boolean completed = results.stream()
                .noneMatch(result -> result == ScrobbleResult.FAILURE);

        log.info("Status: [{}], completed: {}", results.stream().map(ScrobbleResult::name).collect(Collectors.joining(", ")), completed);

        return completed;
    }
}
