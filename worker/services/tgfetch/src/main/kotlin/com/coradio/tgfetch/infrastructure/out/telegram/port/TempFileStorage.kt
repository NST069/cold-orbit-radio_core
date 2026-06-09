package com.coradio.tgfetch.infrastructure.out.telegram.port

import java.nio.file.Path

interface TempFileStorage {
    fun createTempFile(name: String, extension: String): Path
    fun delete(path: Path)
}
