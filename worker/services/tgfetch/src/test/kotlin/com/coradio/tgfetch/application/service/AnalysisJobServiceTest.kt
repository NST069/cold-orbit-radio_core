package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.model.AnalysisJob
import com.coradio.tgfetch.domain.model.Track
import com.coradio.tgfetch.domain.model.TrackFile
import com.coradio.tgfetch.domain.port.out.AnalysisJobRepositoryPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.assertNotNull
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class AnalysisJobServiceTest {

    @Mock
    lateinit var analysisJobRepository: AnalysisJobRepositoryPort

    @InjectMocks
    lateinit var analysisJobService: AnalysisJobService

    val trackFile = TrackFile(
        id = UUID.randomUUID(),
        track = Track(title = "title", artist = "artist", duration = 100),
        sha256 = "sha256",
        telegramFileUniqueId = "1234",
        storageKey = "key",
        fileSize = 100,
        mimeType = "flac"
    )

    val analysisJobCreated = AnalysisJob(
        id = UUID.randomUUID(),
        trackFile = trackFile,
        createdAt = OffsetDateTime.now()
    )

    val analysisJobStarted = AnalysisJob(
        id = UUID.randomUUID(),
        trackFile = trackFile,
        createdAt = OffsetDateTime.now().minus(5, ChronoUnit.MINUTES),
        startedAt = OffsetDateTime.now()
    )

    val analysisJobFinished = AnalysisJob(
        id = UUID.randomUUID(),
        trackFile = trackFile,
        createdAt = OffsetDateTime.now().minus(5, ChronoUnit.MINUTES),
        startedAt = OffsetDateTime.now().minus(5, ChronoUnit.MINUTES),
        finishedAt = OffsetDateTime.now()
    )

    @Test
    fun `saveJob should save analysis job`() {
        whenever(analysisJobRepository.save(any())).thenReturn(analysisJobCreated)

        val result = analysisJobService.saveJob(analysisJobCreated)

        assertEquals(analysisJobCreated.id, result.id)
        assertEquals(analysisJobCreated.trackFile, result.trackFile)
        assertEquals(analysisJobCreated.createdAt, result.createdAt)

        verify(analysisJobRepository, times(1)).save(any())
    }

    @Test
    fun `findJobById should return job`() {
        whenever(analysisJobRepository.findById(any())).thenReturn(analysisJobCreated)

        val result = analysisJobService.findJobById(UUID.randomUUID())

        assertNotNull(result)
        assertEquals(analysisJobCreated.id, result.id)
        assertEquals(analysisJobCreated.trackFile, result.trackFile)
        assertEquals(analysisJobCreated.createdAt, result.createdAt)

        verify(analysisJobRepository, times(1)).findById(any())
    }

    @Test
    fun `findAllJobs should return all jobs`() {
        val jobs = listOf(analysisJobCreated, analysisJobStarted, analysisJobFinished)
        whenever(analysisJobRepository.findAll()).thenReturn(jobs)

        val result = analysisJobService.findAllJobs()

        assertNotNull(result)
        assertEquals(jobs.size, result.size)

        verify(analysisJobRepository, times(1)).findAll()
    }

    @Test
    fun `findAllPendingJobs should return all pending jobs`() {
        val pendingJobs = listOf(analysisJobCreated, analysisJobStarted)
        whenever(analysisJobRepository.findAllByStartedAtIsNull()).thenReturn(pendingJobs)

        val result = analysisJobService.findAllPendingJobs()

        assertNotNull(result)
        assertEquals(pendingJobs.size, result.size)

        verify(analysisJobRepository, times(1)).findAllByStartedAtIsNull()
    }

    @Test
    fun `deleteById should remove job`() {
        doNothing().whenever(analysisJobRepository).deleteById(any())

        analysisJobService.deleteById(UUID.randomUUID())

        verify(analysisJobRepository, times(1)).deleteById(any())
    }

}
