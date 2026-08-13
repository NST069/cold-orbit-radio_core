package com.coradio.rotation.domain.context;

import com.coradio.rotation.application.dto.TrackInfo;
import com.coradio.rotation.application.dto.response.NowPlayingResponse;
import com.coradio.rotation.domain.model.TrackQueueItem;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class NowPlayingStateContext {

    private TrackInfo currentTrack;
    private TrackQueueItem currentQueueItem;

    public NowPlayingResponse toDto() {
        if (currentTrack != null)
            return new NowPlayingResponse(
                    currentTrack.id(),
                    currentTrack.title(),
                    currentTrack.artist(),
                    currentTrack.duration(),
                    false
            );
        else return new NowPlayingResponse(
                null,
                null,
                null,
                0,
                false
        );
    }
}
