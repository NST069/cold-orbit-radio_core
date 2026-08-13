package com.coradio.tgfetch.infrastructure.out.persistence.mapper

import com.coradio.tgfetch.domain.enums.AnalysisJobStatus
import com.coradio.tgfetch.domain.enums.TrackFileStatus
import com.coradio.tgfetch.domain.model.AnalysisJob
import com.coradio.tgfetch.domain.model.TelegramPost
import com.coradio.tgfetch.domain.model.Track
import com.coradio.tgfetch.domain.model.TrackFile
import com.coradio.tgfetch.infrastructure.out.persistence.entity.AnalysisJobEntity
import com.coradio.tgfetch.infrastructure.out.persistence.entity.TelegramPostEntity
import com.coradio.tgfetch.infrastructure.out.persistence.entity.TrackEntity
import com.coradio.tgfetch.infrastructure.out.persistence.entity.TrackFileEntity
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

object MockEntities {

    private val trackId = UUID.randomUUID()

    private val trackFileId = UUID.randomUUID()

    private val postId = UUID.randomUUID()

    private val jobId = UUID.randomUUID()

    private val now = Instant.now()

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
        this.etag = "etag"
        this.telegramFileId = "1234"
        this.telegramFileUniqueId = "1234"
        this.fileName = "artist - title.flac"
        this.fileSize = 100
        this.mimeType = "flac"
        this.storageKey = "1234abcd"
        this.status = TrackFileStatus.CREATED
        this.retryCount = 0
        this.lastDownloadAttemptAt = now.minus(5, ChronoUnit.MINUTES)
    }

    val mockTelegramPostEntity = TelegramPostEntity().apply {
        this.id = postId
        this.trackEntity = mockTrackEntity
        this.channelId = 1234L
        this.messageId = 1234L
        this.rawText = "artist - title"
        this.publishedAt = now.minus(5, ChronoUnit.DAYS)
    }

    val mockAnalysisJobEntity = AnalysisJobEntity().apply {
        this.id = jobId
        this.trackFileEntity = mockTrackFileEntity
        this.status = AnalysisJobStatus.COMPLETED
        this.createdAt = now.minus(5, ChronoUnit.MINUTES)
        this.startedAt = now.minus(5, ChronoUnit.MINUTES)
        this.finishedAt = now
        this.errorMessage = "error"
    }

    val mockTrack = Track(
        id = trackId,
        title = "title",
        artist = "artist",
        duration = 100,
    )

    val mockTrackFile = TrackFile(
        id = trackFileId,
        etag = "etag",
        telegramFileId = "1234",
        telegramFileUniqueId = "1234",
        fileName = "artist - title.flac",
        fileSize = 100,
        mimeType = "flac",
        storageKey = "1234abcd",
        status = TrackFileStatus.CREATED,
        retryCount = 0,
        lastDownloadAttemptAt = now.minus(5, ChronoUnit.MINUTES),
    )

    val mockTelegramPost = TelegramPost(
        id = postId,
        trackId = trackId,
        channelId = 1234L,
        messageId = 1234L,
        rawText = "artist - title",
        publishedAt = now.minus(5, ChronoUnit.DAYS),
    )

    val mockAnalysisJob = AnalysisJob(
        id = jobId,
        trackFile = mockTrackFile,
        status = AnalysisJobStatus.COMPLETED,
        createdAt = now.minus(5, ChronoUnit.MINUTES),
        startedAt = now.minus(5, ChronoUnit.MINUTES),
        finishedAt = now,
        errorMessage = "error",
    )
}
