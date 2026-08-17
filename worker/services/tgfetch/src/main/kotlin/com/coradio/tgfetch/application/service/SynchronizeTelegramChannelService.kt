package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.application.util.MetadataResolver
import com.coradio.tgfetch.domain.model.valueobject.SynchronizationSummary
import com.coradio.tgfetch.domain.model.TelegramPost
import com.coradio.tgfetch.domain.model.Track
import com.coradio.tgfetch.domain.model.TrackFile
import com.coradio.tgfetch.domain.port.`in`.SynchronizeTelegramChannelUseCase
import com.coradio.tgfetch.domain.port.out.persistence.TelegramPostRepositoryPort
import com.coradio.tgfetch.domain.port.out.persistence.TrackRepositoryPort
import com.coradio.tgfetch.domain.port.out.telegram.TelegramGatewayPort
import com.coradio.tgfetch.domain.port.out.telegram.TelegramTrackData
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
class SynchronizeTelegramChannelService(
    private val telegramGateway: TelegramGatewayPort,
    private val trackRepository: TrackRepositoryPort,
    private val telegramPostRepository: TelegramPostRepositoryPort,
    private val metadataResolver: MetadataResolver,
): SynchronizeTelegramChannelUseCase {

    private val log = logger {}
    private lateinit var summary: SynchronizationSummary

    override fun execute(channelId: Long, limit: Int): SynchronizationSummary {
        log.info { "Starting telegram synchronization" }
        val startedAt = Instant.now()

        summary = SynchronizationSummary()

        var cursor = 0L
        var hasMore = true

        while (hasMore) {

            val page = telegramGateway.getMessages(
                channelId = channelId,
                limit = limit,
                cursor = cursor
            )

            val items = page.items

            if (items.isEmpty()) break

            items.filter { !it.remoteFileId.isBlank() }.forEach { telegramPost ->
                val existingPost =
                    telegramPostRepository.findByChannelAndMessageId(telegramPost.channelId, telegramPost.messageId)

                if (existingPost == null) {
                    createNewTrack(telegramPost)
                    return@forEach
                }
                existingPost.trackId
                    ?.let { trackRepository.findById(it) }
                    ?.let { track -> updateTrack(track, telegramPost) }
            }

            cursor = page.nextCursor ?: 0

            hasMore = page.hasMore
        }

        val duration = Duration.between(
            startedAt,
            Instant.now()
        )
        log.info { "Synchronization completed in ${duration.toSeconds()}s. Summary: $summary" }

        return summary
    }

    @Transactional
    fun createNewTrack(telegramTrackData: TelegramTrackData): Track {

        val metadata = metadataResolver.resolve(telegramTrackData)

        val track = Track(
            title = metadata.title,
            artist = metadata.artist,
            duration = telegramTrackData.durationSeconds ?: 0,
        )

        val trackFile = TrackFile(
            etag = UUID.randomUUID().toString(),
            telegramFileId = telegramTrackData.remoteFileId,
            telegramFileUniqueId = telegramTrackData.uniqueFileId,
            fileName = telegramTrackData.fileName ?: "",
            fileSize = telegramTrackData.fileSizeBytes ?: 0,
            mimeType = telegramTrackData.mimeType ?: "",
        )

        val telegramPost = TelegramPost(
            channelId = telegramTrackData.channelId,
            messageId = telegramTrackData.messageId,
            rawText = telegramTrackData.rawText ?: "",
            publishedAt = telegramTrackData.publishedAt,
        )

        track.attachTelegramPost(telegramPost)
        track.attachTrackFile(trackFile)

        log.debug { "Created new track: ${track.artist} by ${track.title}" }
        summary.created++

        return trackRepository.save(track)
    }

    private fun updateTrack(existingTrack: Track, incomingPost: TelegramTrackData): Track? {

        if (existingTrack.syncWith(incomingPost)) {
            log.debug { "Updated track: ${existingTrack.artist} by ${existingTrack.title}" }
            summary.updated++

            return trackRepository.save(existingTrack)
        } else
            summary.skipped++

        return null
    }

}
