package com.coradio.rotation.domain.port.in;

import com.coradio.rotation.application.dto.response.NowPlayingResponse;
import com.coradio.rotation.application.dto.response.RadioInfoResponse;

public interface RadioInfoUseCase {

    NowPlayingResponse getNowPlaying();

    RadioInfoResponse getRadioInfo();
}
