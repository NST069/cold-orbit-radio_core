package com.coradio.rotation.domain.enums;

import com.coradio.rotation.application.exception.UnknownLiquidsoapEventException;
import lombok.Getter;
import java.util.Arrays;

@Getter
public enum LiquidsoapEvent {
    TRACK_START("track_start"),
    TRACK_END("track_end"),
    TRACK_SCROBBLE("track_scrobble"),
    QUEUE_EMPTY("queue_empty");

    private final String value;

    LiquidsoapEvent(String value) {
        this.value = value;
    }

    public static LiquidsoapEvent fromValue(String value) {
        return Arrays.stream(values())
                .filter(e -> e.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new UnknownLiquidsoapEventException("Unknown event: " + value));
    }

}
