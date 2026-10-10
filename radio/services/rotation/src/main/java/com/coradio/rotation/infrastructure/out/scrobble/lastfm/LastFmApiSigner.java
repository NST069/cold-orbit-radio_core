package com.coradio.rotation.infrastructure.out.scrobble.lastfm;

import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LastFmApiSigner {

    public String sign(
            Map<String, String> params,
            String apiSecret
    ) {
        String signatureSource = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + entry.getValue())
                .collect(Collectors.joining());

        return DigestUtils.md5DigestAsHex(
                (signatureSource + apiSecret)
                        .getBytes(StandardCharsets.UTF_8)
        );
    }
}
