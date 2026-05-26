package com.coradio.tgfetch.domain.port.out.storage

import java.nio.file.Path

interface AudioMetadataServicePort {

    fun rewriteMetadata(
        file: Path,
        artist: String,
        title: String
    )
}
