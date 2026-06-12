package com.coradio.rotation.domain.port.out.persistence;

import com.coradio.rotation.application.dto.TrackInfo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrackCatalogPort {

    Optional<TrackInfo> findById(UUID trackId);

    List<TrackInfo> findPlayableTracks();
}
