package com.coradio.rotation.infrastructure.out.dj;

import com.coradio.rotation.application.dto.TrackInfo;
import com.coradio.rotation.domain.model.PlaybackHistoryItem;
import com.coradio.rotation.domain.port.out.dj.TrackSelectionStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RandomTrackSelectionStrategy implements TrackSelectionStrategy {

    @Override
    public List<TrackInfo> selectTracks(List<TrackInfo> candidates, int count, List<PlaybackHistoryItem> history) {
        if (candidates.isEmpty()) {
            return List.of();
        }

        List<TrackInfo> shuffled = new ArrayList<>(candidates);

        Collections.shuffle(shuffled);

        return shuffled.stream()
                .limit(count > 0 ? count : 1)
                .toList();
    }
}
