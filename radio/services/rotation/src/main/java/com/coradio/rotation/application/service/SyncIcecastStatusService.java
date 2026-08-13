package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.context.IcecastStateContext;
import com.coradio.rotation.domain.port.in.SyncIcecastStatusUseCase;
import com.coradio.rotation.domain.port.out.icecast.IcecastClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncIcecastStatusService implements SyncIcecastStatusUseCase {

    private final IcecastClientPort icecastClientPort;

    private final IcecastStateContext icecastStateContext;

    @Override
    public void sync() {
        icecastStateContext.setStationInfo(icecastClientPort.fetchStationInfo());
    }

}
