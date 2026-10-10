package com.coradio.rotation.domain.port.in;

import com.coradio.rotation.domain.context.RecentTrack;
import java.util.List;

public interface LastPlayedUseCase {

    List<RecentTrack> getLastPlayed();
}
