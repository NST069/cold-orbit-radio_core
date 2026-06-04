package com.coradio.tgfetch.domain.model.mapper

object ExtensionMapper {

    fun mimeToExt(mime: String): String {
        return when (mime.lowercase()) {
            "audio/flac", "audio/x-flac" -> "flac"
            "audio/mpeg" -> "mp3"
            "audio/mp4", "audio/x-m4a" -> "m4a"
            "audio/ogg", "audio/opus" -> "ogg"
            "audio/wav", "audio/wave" -> "wav"

            "video/mp4" -> "mp4"
            "image/jpeg" -> "jpg"
            "image/png" -> "png"

            else -> "bin"
        }
    }

}
