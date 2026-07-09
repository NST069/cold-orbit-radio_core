package com.coradio.rotation.domain.enums;

import lombok.Getter;
import java.util.Arrays;

@Getter
public enum LiquidsoapEvent {
    TRACK_START("track_start"),
    TRACK_END("track_end");

    private final String value;

    LiquidsoapEvent(String value) {
        this.value = value;
    }

    public static LiquidsoapEvent fromValue(String value) {
        return Arrays.stream(values())
                .filter(e -> e.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown value: " + value));
    }

}
