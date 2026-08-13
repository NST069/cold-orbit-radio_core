package com.coradio.tgfetch.infrastructure.out.storage.temp

import com.coradio.tgfetch.infrastructure.out.telegram.port.TempFileStorage
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Component
class LocalTempFileStorage : TempFileStorage {

    override fun createTempFile(name: String, extension: String): Path {
        return Paths.get(System.getProperty("java.io.tmpdir"))
            .resolve("$name-${System.nanoTime()}.$extension")
    }

    override fun delete(path: Path) {
        Files.deleteIfExists(path)
    }
}
