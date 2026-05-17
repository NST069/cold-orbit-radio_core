package com.coradio.tgfetch.infrastructure.persistence.mapper

import com.coradio.tgfetch.domain.model.AnalysisJob
import com.coradio.tgfetch.domain.model.TelegramPost
import com.coradio.tgfetch.domain.model.Track
import com.coradio.tgfetch.domain.model.TrackFile
import com.coradio.tgfetch.infrastructure.persistence.entity.AnalysisJobEntity
import com.coradio.tgfetch.infrastructure.persistence.entity.TelegramPostEntity
import com.coradio.tgfetch.infrastructure.persistence.entity.TrackEntity
import com.coradio.tgfetch.infrastructure.persistence.entity.TrackFileEntity
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

object MockEntities {

    private val trackId = UUID.randomUUID()

    private val trackFileId = UUID.randomUUID()

    private val postId = UUID.randomUUID()

    private val jobId = UUID.randomUUID()

    private val now = OffsetDateTime.now()

    val mockTrackEntity = TrackEntity().apply {
        this.id = trackId
        this.artist = "artist"
        this.title = "title"
        this.duration = 100
        this.createdAt = now.minus(5, ChronoUnit.MINUTES)
        this.updatedAt = now.minus(5, ChronoUnit.MINUTES)
    }

    val mockTrackFileEntity = TrackFileEntity().apply {
        this.id = trackFileId
        this.sha256 = "sha256"
        this.trackEntity = mockTrackEntity
        this.telegramFileUniqueId = "1234"
        this.mimeType = "flac"
        this.fileSize = 100
    }

    val mockTelegramPostEntity = TelegramPostEntity().apply {
        this.id = postId
        this.telegramPostId = "1234"
        this.trackEntity = mockTrackEntity
        this.trackFileEntity = mockTrackFileEntity
        this.rawText = "artist - title"
        this.fetchedAt = now.minus(5, ChronoUnit.MINUTES)
    }

    val mockAnalysisJobEntity = AnalysisJobEntity().apply {
        this.id = jobId
        this.trackFileEntity = mockTrackFileEntity
        this.createdAt = now.minus(5, ChronoUnit.MINUTES)
        this.startedAt = now.minus(5, ChronoUnit.MINUTES)
        this.finishedAt = now
        this.errorMessage = "error"
    }

    val mockTrack = Track(
        id = trackId,
        title = "title",
        artist = "artist",
        duration = 100
    )

    val mockTrackFile = TrackFile(
        id = trackFileId,
        sha256 = "sha256",
        track = mockTrack,
        telegramFileUniqueId = "1234",
        fileSize = 100,
        mimeType = "flac",
    )

    val mockAnalysisJob = AnalysisJob(
        id = jobId,
        trackFile = mockTrackFile,
        createdAt = now.minus(5, ChronoUnit.MINUTES),
        startedAt = now.minus(5, ChronoUnit.MINUTES),
        finishedAt = now,
        errorMessage = "error"
    )

    val mockTelegramPost = TelegramPost(
        id = postId,
        telegramPostId = "1234",
        track = mockTrack,
        trackFile = mockTrackFile,
        rawText = "artist - title"
    )
}
