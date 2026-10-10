package com.coradio.rotation.domain.context;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TrackStatsDelta {
    private long plays;
    private long likes;

    public void incrementPlayCount(){
        this.plays++;
    }
}
