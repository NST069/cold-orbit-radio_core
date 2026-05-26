package com.coradio.tgfetch.application.util

import com.coradio.tgfetch.domain.model.TrackMetadata
import com.coradio.tgfetch.domain.port.out.telegram.TelegramTrackData
import org.springframework.stereotype.Component

@Component
class MetadataResolver {

    fun resolve(data: TelegramTrackData): TrackMetadata {

        resolveFromCaption(data.rawText)?.let {
            return it
        }

        resolveFromTelegramMetadata(data)?.let {
            return it
        }

        resolveFromFileName(data.fileName)?.let {
            return it
        }

        return TrackMetadata(
            artist = "<Неизвестен>",
            title = "<Трек не распознан>"
        )

    }

    private fun resolveFromCaption(
        rawText: String?
    ): TrackMetadata? {
        if (rawText.isNullOrBlank()) return null

        val firstLine = rawText.lineSequence().firstOrNull()
            ?: return null

        return parseArtistTitle(firstLine)
    }

    private fun resolveFromTelegramMetadata(
        data: TelegramTrackData
    ): TrackMetadata? {

        if (data.artist.isNullOrBlank() &&
            data.title.isNullOrBlank()
        ) {
            return null
        }

        return TrackMetadata(
            artist = data.artist?.trim().orEmpty()
                .ifBlank { "<Неизвестен>" },

            title = data.title?.trim().orEmpty()
                .ifBlank { "<Без названия>" }
        )
    }

    private fun resolveFromFileName(
        fileName: String?
    ): TrackMetadata? {

        if (fileName.isNullOrBlank()) {
            return null
        }

        val nameWithoutExtension =
            fileName.substringBeforeLast('.')

        return parseArtistTitle(nameWithoutExtension)
    }

    private fun parseArtistTitle(
        source: String
    ): TrackMetadata? {

        val parts =
            source.split(Regex("\\s+[\\-–—]\\s+"))

        if (parts.size < 2) {
            return null
        }

        return TrackMetadata(
            artist = parts[0].trim(),
            title = parts.drop(1).joinToString(" - ").trim()
        )
    }
}
