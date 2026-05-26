package com.coradio.tgfetch.infrastructure.storage

import com.coradio.tgfetch.domain.port.out.storage.AudioMetadataServicePort
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class AudioMetadataServiceAdapter: AudioMetadataServicePort {
    override fun rewriteMetadata(file: Path, artist: String, title: String) {
        TODO("Not yet implemented")
    }
}
