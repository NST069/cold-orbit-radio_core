package com.coradio.tgfetch.infrastructure.out.storage

import com.coradio.tgfetch.infrastructure.exception.AudioMetadataException
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.nio.file.Files
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class JaudiotaggerAudioMetadataServiceAdapterTest {

    @InjectMocks
    lateinit var service: JaudiotaggerAudioMetadataServiceAdapter

    @Test
    fun `should rewrite metadata when tag exists`() {

        val file = Files.createTempFile("test", ".mp3")

        val audioFile = mock(AudioFile::class.java)
        val tag = mock(Tag::class.java)

        whenever(audioFile.tag).thenReturn(tag)

        mockStatic(AudioFileIO::class.java).use { mocked ->

            mocked.`when`<AudioFile> {
                AudioFileIO.read(file.toFile())
            }.thenReturn(audioFile)

            service.rewriteMetadata(file, "Artist", "Title")

            verify(tag).setField(FieldKey.ARTIST, "Artist")
            verify(tag).setField(FieldKey.TITLE, "Title")
            verify(audioFile).commit()
        }
    }

    @Test
    fun `should create default tag when tag is null`() {

        val file = Files.createTempFile("test", ".mp3")

        val audioFile = mock(AudioFile::class.java)
        val tag = mock(Tag::class.java)

        whenever(audioFile.tag).thenReturn(null)
        whenever(audioFile.createDefaultTag()).thenReturn(tag)

        mockStatic(AudioFileIO::class.java).use { mocked ->

            mocked.`when`<AudioFile> {
                AudioFileIO.read(file.toFile())
            }.thenReturn(audioFile)

            service.rewriteMetadata(file, "Artist", "Title")

            verify(audioFile).createDefaultTag()
            verify(audioFile).tag = tag

            verify(tag).setField(FieldKey.ARTIST, "Artist")
            verify(tag).setField(FieldKey.TITLE, "Title")

            verify(audioFile).commit()
        }
    }

    @Test
    fun `should throw AudioMetadataException when tagging fails`() {

        val file = Files.createTempFile("test", ".mp3")

        mockStatic(AudioFileIO::class.java).use { mocked ->

            mocked.`when`<AudioFile> {
                AudioFileIO.read(file.toFile())
            }.thenThrow(RuntimeException("boom"))

            val ex = assertThrows<AudioMetadataException> {
                service.rewriteMetadata(file, "Artist", "Title")
            }

            assertTrue(ex.message!!.contains("Failed to rewrite metadata"))
            assertNotNull(ex.cause)
        }
    }

}
