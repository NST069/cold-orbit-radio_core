package com.coradio.tgfetch.infrastructure.out.storage.temp

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class LocalTempFileStorageTest {

    @InjectMocks
    lateinit var storage: LocalTempFileStorage

    @Test
    fun `should create temp file with correct pattern`() {

        val path = storage.createTempFile("track", "mp3")

        assertTrue(path.toString().contains("track-"))
        assertTrue(path.toString().endsWith(".mp3"))

        assertTrue(
            path.startsWith(Paths.get(System.getProperty("java.io.tmpdir")))
        )
    }

    @Test
    fun `should generate unique file names`() {

        val p1 = storage.createTempFile("track", "mp3")
        val p2 = storage.createTempFile("track", "mp3")

        assertNotEquals(p1, p2)
    }

    @Test
    fun `should delete file if exists`() {

        val path = Files.createTempFile("test", ".tmp")

        storage.delete(path)

        assertFalse(Files.exists(path))
    }

    @Test
    fun `should not fail when file does not exist`() {

        val path = Paths.get("non-existing-file.tmp")

        storage.delete(path)

        assertFalse(Files.exists(path))
    }

}
