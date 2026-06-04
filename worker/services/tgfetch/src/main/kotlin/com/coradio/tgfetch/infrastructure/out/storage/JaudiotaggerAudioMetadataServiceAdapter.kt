package com.coradio.tgfetch.infrastructure.out.storage

import com.coradio.tgfetch.domain.port.out.storage.AudioMetadataServicePort
import com.coradio.tgfetch.infrastructure.exception.AudioMetadataException
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class JaudiotaggerAudioMetadataServiceAdapter: AudioMetadataServicePort {

    private val log = logger {}

    override fun rewriteMetadata(file: Path, artist: String, title: String) {
        try{
            val audioFile = AudioFileIO.read(file.toFile())

            var tag = audioFile.tag

            if(tag == null){
                tag = audioFile.createDefaultTag()
                audioFile.tag = tag
            }

            tag.setField(FieldKey.ARTIST, artist)
            tag.setField(FieldKey.TITLE, title)

            audioFile.commit()
        } catch (e: Exception) {
            log.error(e) { "Failed to rewrite metadata: artist - $artist, title - $title" }
            throw AudioMetadataException(
                "Failed to rewrite metadata",
                e
            )
        }
    }
}
