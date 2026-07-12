package com.coradio.rotation.infrastructure.out.scrobble.librefm;

import com.coradio.rotation.infrastructure.out.scrobble.librefm.dto.LibreFmSession;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class LibreFmSessionHolder {

    private LibreFmSession session;
}
