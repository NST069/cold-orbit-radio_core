package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.port.in.ScrobbleNowPlayingUseCase;
import com.coradio.rotation.domain.port.out.scrobbler.ScrobbleProviderPort;
import com.coradio.rotation.domain.port.out.scrobbler.ScrobbleProviderRegistryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScrobbleNowPlayingService implements ScrobbleNowPlayingUseCase {

    private final ScrobbleProviderRegistryPort registry;

    @Override
    public void update(PlaybackHistoryItem historyItem) {

        registry.getProviders()
                .stream()
                .filter(ScrobbleProviderPort::supportsNowPlaying)
                .forEach(provider -> {
                    try {
                        log.debug("Updating nowPlaying for {} to {}", provider.provider(), historyItem.artist() + " - " + historyItem.title());

                        provider.updateNowPlaying(historyItem);
                    } catch (Exception ex) {
                        log.error("Error updating nowPlaying for {}", provider.provider(), ex);
                    }
                });

    }

}
