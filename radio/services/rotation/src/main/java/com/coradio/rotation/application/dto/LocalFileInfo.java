package com.coradio.rotation.application.dto;

import java.nio.file.Path;
import java.time.Instant;

public record LocalFileInfo(
        Path path,
        Instant lastModified
) {
}
