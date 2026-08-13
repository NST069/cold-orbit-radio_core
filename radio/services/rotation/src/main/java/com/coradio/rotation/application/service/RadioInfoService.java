package com.coradio.rotation.application.service;

import com.coradio.rotation.application.dto.response.NowPlayingResponse;
import com.coradio.rotation.application.dto.response.RadioInfoResponse;
import com.coradio.rotation.domain.context.IcecastStateContext;
import com.coradio.rotation.domain.context.NowPlayingStateContext;
import com.coradio.rotation.domain.port.in.RadioInfoUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RadioInfoService implements RadioInfoUseCase {

    private final NowPlayingStateContext nowPlayingStateContext;

    private final IcecastStateContext icecastStateContext;

    @Override
    public NowPlayingResponse getNowPlaying() {
        return nowPlayingStateContext.toDto();
    }

    @Override
    public RadioInfoResponse getRadioInfo() {
        return icecastStateContext.toDto();
    }

}
