package com.coradio.rotation.domain.port.in;

import com.coradio.rotation.application.dto.response.PlaybackHistoryItemDto;
import java.util.List;

public interface LastPlayedUseCase {

    List<PlaybackHistoryItemDto> getLastPlayed();
}
