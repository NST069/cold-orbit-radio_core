package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.application.util.MetadataResolver
import com.coradio.tgfetch.domain.model.TelegramPost
import com.coradio.tgfetch.domain.model.Track
import com.coradio.tgfetch.domain.model.TrackFile
import com.coradio.tgfetch.domain.port.`in`.SynchronizeTelegramChannelUseCase
import com.coradio.tgfetch.domain.port.out.persistence.TelegramPostRepositoryPort
import com.coradio.tgfetch.domain.port.out.telegram.TelegramGatewayPort
import com.coradio.tgfetch.domain.port.out.telegram.TelegramTrackData
import java.util.UUID

class SynchronizeTelegramChannelService(
    private val telegramGateway: TelegramGatewayPort,
    private val telegramPostRepository: TelegramPostRepositoryPort,
    private val metadataResolver: MetadataResolver,
): SynchronizeTelegramChannelUseCase {
    override suspend fun execute() {
        val telegramPosts = telegramGateway.getTrackPosts()

        telegramPosts.forEach { telegramPost ->
            val existingPost =
                telegramPostRepository.findByChannelAndMessageId(telegramPost.channelId, telegramPost.messageId)

            if (existingPost == null) {
                createNewTrack(telegramPost)
                return@forEach
            }
            updateTrack(existingPost, telegramPost)
        }
    }

    private fun createNewTrack(telegramTrackData: TelegramTrackData): TelegramPost {

        val metadata = metadataResolver.resolve(telegramTrackData)

        val track = Track(
            title = metadata.title,
            artist = metadata.artist,
            duration = telegramTrackData.durationSeconds ?: 0,
        )

        val trackFile = TrackFile(
            track = track,
            etag = UUID.randomUUID().toString(),
            telegramFileId = telegramTrackData.fileId ?: "",
            telegramFileUniqueId = telegramTrackData.fileUniqueId ?: "",
            fileSize = telegramTrackData.fileSizeBytes ?: 0,
            mimeType = telegramTrackData.mimeType ?: "",
        )

        val telegramPost = TelegramPost(
            channelId = telegramTrackData.channelId,
            messageId = telegramTrackData.messageId,
            track = track,
            trackFile = trackFile,
            rawText = telegramTrackData.rawText ?: "",
            publishedAt = telegramTrackData.publishedAt,
        )

        return telegramPostRepository.save(telegramPost)
    }

    private fun updateTrack(existingPost: TelegramPost, incomingPost: TelegramTrackData): TelegramPost? {
        if (existingPost.trackFile.telegramFileUniqueId != incomingPost.fileUniqueId) {
            val track = Track(
                title = incomingPost.title ?: "",
                artist = incomingPost.artist ?: "",
                duration = incomingPost.durationSeconds ?: 0,
            )

            val trackFile = TrackFile(
                track = track,
                etag = UUID.randomUUID().toString(),
                telegramFileId = incomingPost.fileId,
                telegramFileUniqueId = incomingPost.fileUniqueId,
                fileSize = incomingPost.fileSizeBytes ?: 0,
                mimeType = incomingPost.mimeType ?: "",
            )

            existingPost.changeTrack(track, trackFile)

            return telegramPostRepository.save(existingPost)
        }
        return null
    }

}
