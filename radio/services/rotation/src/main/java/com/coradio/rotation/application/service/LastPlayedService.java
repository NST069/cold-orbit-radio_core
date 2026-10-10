package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.context.RecentTrack;
import com.coradio.rotation.domain.context.RecentTracksStateContext;
import com.coradio.rotation.domain.port.in.LastPlayedUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LastPlayedService implements LastPlayedUseCase {

    private final RecentTracksStateContext recentTracksStateContext;

    @Override
    public List<RecentTrack> getLastPlayed() {
        return recentTracksStateContext.getRecentHistory();
    }
}
