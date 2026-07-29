package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.context.IcecastStateContext;
import com.coradio.rotation.domain.context.StationInfo;
import com.coradio.rotation.domain.port.out.icecast.IcecastClientPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncIcecastStatusServiceTest {

    @Mock
    private IcecastClientPort icecastClientPort;

    @Mock
    private IcecastStateContext icecastStateContext;

    @InjectMocks
    private SyncIcecastStatusService service;

    @Test
    void shouldSyncStationInfo() {

        StationInfo stationInfo = new StationInfo(
                "Cold Orbit Radio",
                "Interplanetary broadcast station",
                "Various",
                "http://localhost:8000/radio.mp3",
                "KTRSS - ATLAS",
                5,
                12,
                Instant.now()
        );

        when(icecastClientPort.fetchStationInfo()).thenReturn(stationInfo);

        service.sync();

        verify(icecastClientPort).fetchStationInfo();
        verify(icecastStateContext).setStationInfo(stationInfo);
        verifyNoMoreInteractions(icecastClientPort, icecastStateContext);
    }

    @Test
    void shouldPropagateException() {

        RuntimeException ex = new RuntimeException("Icecast unavailable");

        when(icecastClientPort.fetchStationInfo()).thenThrow(ex);

        assertThatThrownBy(service::sync).isSameAs(ex);

        verify(icecastClientPort).fetchStationInfo();
        verifyNoInteractions(icecastStateContext);
    }
}
