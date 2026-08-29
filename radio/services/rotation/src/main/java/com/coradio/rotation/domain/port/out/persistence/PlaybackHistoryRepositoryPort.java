package com.coradio.rotation.domain.port.out.persistence;

import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import java.util.List;
import java.util.Optional;

public interface PlaybackHistoryRepositoryPort {
    PlaybackHistoryItem save(PlaybackHistoryItem playbackHistoryItem);

    List<PlaybackHistoryItem> findAllInRange(long hours);

    Optional<PlaybackHistoryItem> findLatestByArtistAndTitle(String artist, String title);

    List<PlaybackHistoryItem> findLast10PlayedTracks();

}
