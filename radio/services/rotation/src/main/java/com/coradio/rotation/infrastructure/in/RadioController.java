package com.coradio.rotation.infrastructure.in;

import com.coradio.rotation.application.dto.response.NowPlayingResponse;
import com.coradio.rotation.application.dto.response.RadioInfoResponse;
import com.coradio.rotation.domain.port.in.RadioInfoUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/")
public class RadioController {

    private final RadioInfoUseCase radioInfoService;

    @GetMapping("/now-playing")
    public ResponseEntity<NowPlayingResponse> nowPlaying() {
        return ResponseEntity.ok(radioInfoService.getNowPlaying());
    }

    @GetMapping
    public ResponseEntity<RadioInfoResponse> radioInfo() {
        return ResponseEntity.ok(radioInfoService.getRadioInfo());
    }

}
