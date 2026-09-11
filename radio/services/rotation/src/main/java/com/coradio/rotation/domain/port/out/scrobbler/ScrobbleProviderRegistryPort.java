package com.coradio.rotation.domain.port.out.scrobbler;

import com.coradio.rotation.domain.enums.ScrobblerProvider;
import java.util.List;

@Deprecated
public interface ScrobbleProviderRegistryPort {

    ScrobbleProviderPort get(ScrobblerProvider provider);

    List<ScrobbleProviderPort> getProviders();

}
