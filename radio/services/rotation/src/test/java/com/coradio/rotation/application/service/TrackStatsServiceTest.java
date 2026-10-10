package com.coradio.rotation.application.service;

import com.coradio.rotation.domain.context.TrackStatsStateContext;
import com.coradio.rotation.domain.port.out.persistence.TrackStatsRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class TrackStatsServiceTest {

    @Mock
    private TrackStatsStateContext state;

    @Mock
    private TrackStatsRepositoryPort trackStatsRepository;

    @InjectMocks
    private TrackStatsService service;

    @Test
    void shouldActualizeTrackStats() {
        service.flush();

        verify(trackStatsRepository).actualizeTrackStats(state);
    }

    @Test
    void shouldNotInteractWithStateDirectly() {
        service.flush();

        verifyNoInteractions(state);
    }

    @Test
    void shouldCallRepositoryOnlyOnce() {
        service.flush();

        verify(trackStatsRepository, times(1)).actualizeTrackStats(state);
        verifyNoMoreInteractions(trackStatsRepository);
    }
}
