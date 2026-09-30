package com.coradio.notification.domain.port.out.scrobbler;

import com.coradio.notification.domain.port.enums.ScrobblerProvider;
import java.util.List;

public interface ScrobbleProviderRegistryPort {

    ScrobbleProviderPort get(ScrobblerProvider provider);

    List<ScrobbleProviderPort> getProviders();

}
