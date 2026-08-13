package com.coradio.rotation.application.service;

import com.coradio.rotation.application.exception.PlaybackPreparationException;
import com.coradio.rotation.domain.enums.PlaybackStatus;
import com.coradio.rotation.domain.port.in.MonitorPlaybackUseCase;
import com.coradio.rotation.domain.port.in.PlaybackResyncUseCase;
import com.coradio.rotation.domain.port.out.liquidsoap.PlaybackEnginePort;
import com.coradio.rotation.domain.port.out.persistence.TrackQueueRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MonitorPlaybackService implements MonitorPlaybackUseCase {

    private final PlaybackEnginePort playbackEngine;

    private final TrackQueueRepositoryPort trackQueueRepository;

    private final PlaybackResyncUseCase playbackResyncService;

    @Override
    public void monitorPlayback() {
        try {
            if (playbackEngine.getQueueLength() == 0 && trackQueueRepository.existsByStatusIn(List.of(PlaybackStatus.PLAYING, PlaybackStatus.QUEUED))) {
                playbackResyncService.resync();
            }
        } catch (PlaybackPreparationException ex) {
            log.error("Error while monitoring playback", ex);
        }
    }

}
