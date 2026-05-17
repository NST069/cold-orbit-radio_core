package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.model.TelegramPost
import com.coradio.tgfetch.domain.model.Track
import com.coradio.tgfetch.domain.model.TrackFile
import com.coradio.tgfetch.domain.port.out.TelegramPostRepositoryPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.times
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class TelegramPostServiceTest {

    @Mock
    lateinit var telegramPostRepository: TelegramPostRepositoryPort

    @InjectMocks
    lateinit var telegramPostService: TelegramPostService

    val track = Track(
        id = UUID.randomUUID(),
        title = "title",
        artist = "artist",
        duration = 100
    )

    val trackFile = TrackFile(
        id = UUID.randomUUID(),
        track = track,
        sha256 = "sha256",
        telegramFileUniqueId = "1234",
        storageKey = "key",
        fileSize = 100,
        mimeType = "flac"
    )

    val telegramPost = TelegramPost(
        id = UUID.randomUUID(),
        telegramPostId = "1234",
        track = track,
        trackFile = trackFile,
        rawText = "${track.artist} - ${track.title}"
    )

    @Test
    fun `saveTelegramPost should save a new post`() {
        whenever(telegramPostRepository.save(any())).thenReturn(telegramPost)

        val result = telegramPostService.saveTelegramPost(telegramPost)

        assertEquals(telegramPost.id, result.id)
        assertEquals(telegramPost.telegramPostId, result.telegramPostId)
        assertEquals(telegramPost.track, result.track)
        assertEquals(telegramPost.trackFile, result.trackFile)
        assertEquals(telegramPost.rawText, result.rawText)

        verify(telegramPostRepository, times(1)).save(any())
    }

    @Test
    fun `findPostById should return post`() {
        whenever(telegramPostRepository.findById(any())).thenReturn(telegramPost)

        val result = telegramPostService.findPostById(UUID.randomUUID())

        assertNotNull(result)
        assertEquals(telegramPost.id, result.id)
        assertEquals(telegramPost.telegramPostId, result.telegramPostId)
        assertEquals(telegramPost.track, result.track)
        assertEquals(telegramPost.trackFile, result.trackFile)
        assertEquals(telegramPost.rawText, result.rawText)

        verify(telegramPostRepository, times(1)).findById(any())
    }

    @Test
    fun `findAllPosts should return all posts`() {
        val telegramPosts = listOf(telegramPost, telegramPost)
        whenever(telegramPostRepository.findAll()).thenReturn(telegramPosts)

        val result = telegramPostService.findAllPosts()

        assertNotNull(result)
        assertEquals(telegramPosts.size, result.size)

        verify(telegramPostRepository, times(1)).findAll()
    }

    @Test
    fun `deleteById should remove post`() {
        doNothing().whenever(telegramPostRepository).deleteById(any())

        telegramPostService.deleteById(UUID.randomUUID())

        verify(telegramPostRepository, times(1)).deleteById(any())
    }

}
