package com.coradio.rotation.infrastructure.in;

import com.coradio.rotation.application.dto.request.LiquidsoapRequest;
import com.coradio.rotation.domain.port.in.PlaybackEventUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/webhooks")
public class WebhooksController {

    private final PlaybackEventUseCase playbackEventService;

    @PostMapping("/liquidsoap")
    public ResponseEntity<String> liquidsoapEvent(@Valid @RequestBody LiquidsoapRequest request) {
        log.debug("Liquidsoap Event: {}", request);
        playbackEventService.handleLiquidsoapEvent(request);
        return ResponseEntity.ok().build();
    }

}
