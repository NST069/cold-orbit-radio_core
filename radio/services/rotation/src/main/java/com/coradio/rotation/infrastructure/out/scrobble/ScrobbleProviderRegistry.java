package com.coradio.rotation.infrastructure.out.scrobble;

import com.coradio.rotation.domain.enums.ScrobblerProvider;
import com.coradio.rotation.domain.port.out.scrobbler.ScrobbleProviderPort;
import com.coradio.rotation.domain.port.out.scrobbler.ScrobbleProviderRegistryPort;
import com.coradio.rotation.infrastructure.exception.ScrobbleProviderNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScrobbleProviderRegistry implements ScrobbleProviderRegistryPort {

    private final List<ScrobbleProviderPort> providers;

    private Map<ScrobblerProvider, ScrobbleProviderPort> map;

    @PostConstruct
    void init() {
        map = providers.stream()
                .filter(ScrobbleProviderPort::enabled)
                .collect(Collectors.toMap(
                        ScrobbleProviderPort::provider,
                        Function.identity()
                ));
    }

    public ScrobbleProviderPort get(ScrobblerProvider provider) {
        return Optional.ofNullable(map.get(provider))
                .orElseThrow(() -> new ScrobbleProviderNotFoundException(provider.name()));
    }

    @Override
    public List<ScrobbleProviderPort> getProviders() {
        return map.values().stream().toList();
    }

}
