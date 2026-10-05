package com.coradio.notification.domain.out.scrobbler;

import com.coradio.notification.domain.enums.ScrobblerProvider;
import java.util.List;

public interface ScrobbleProviderRegistryPort {

    ScrobbleProviderPort get(ScrobblerProvider provider);

    List<ScrobbleProviderPort> getProviders();

}
