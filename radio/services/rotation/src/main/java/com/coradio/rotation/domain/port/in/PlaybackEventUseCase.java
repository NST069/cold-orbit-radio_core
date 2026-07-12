package com.coradio.rotation.domain.port.in;

import com.coradio.rotation.application.dto.request.LiquidsoapRequest;

public interface PlaybackEventUseCase {

    void handleLiquidsoapEvent(LiquidsoapRequest request);
}
