package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.enums.TrackFileStatus
import com.coradio.tgfetch.domain.model.TrackFile
import com.coradio.tgfetch.domain.port.`in`.DownloadPendingTrackFilesUseCase
import com.coradio.tgfetch.domain.port.out.persistence.TrackFileRepositoryPort
import com.coradio.tgfetch.domain.port.out.storage.AudioMetadataServicePort
import com.coradio.tgfetch.domain.port.out.storage.StorageGatewayPort
import com.coradio.tgfetch.domain.port.out.telegram.TelegramGatewayPort
import org.springframework.stereotype.Service
import java.nio.file.Files

@Service
class DownloadPendingTrackFilesService(
    private val trackFileRepository: TrackFileRepositoryPort,
    private val telegramGateway: TelegramGatewayPort,
    private val storageGateway: StorageGatewayPort,
    private val audioMetadataService: AudioMetadataServicePort,
): DownloadPendingTrackFilesUseCase {
    override suspend fun execute() {

        val pendingFiles = trackFileRepository.findAllByStatus(TrackFileStatus.PENDING)

        pendingFiles.forEach { trackFile ->
            try {
                markDownloading(trackFile)
                val file = telegramGateway.downloadFile(trackFile.telegramFileId)
                val storageKey = generateStorageKey(trackFile)
                audioMetadataService.rewriteMetadata(file, trackFile.track.artist, trackFile.track.title)
                storageGateway.upload(storageKey, Files.newInputStream(file))
                markReady(trackFile, storageKey)
                Files.deleteIfExists(file)
            } catch (ex: Exception) {
                markFailed(trackFile)
                //TODO: Log Error
            }
        }

    }

    private fun generateStorageKey(trackFile: TrackFile): String {
        return "tracks/${trackFile.id}.${trackFile.mimeType}"
    }

    private fun markDownloading(trackFile: TrackFile) {
        trackFile.changeStatus(TrackFileStatus.DOWNLOADING)
        trackFileRepository.save(trackFile)
    }

    private fun markReady(trackFile: TrackFile, storageKey: String) {
        trackFile.changeStatus(TrackFileStatus.READY)
        trackFile.addStorageKey(storageKey)
        trackFileRepository.save(trackFile)
    }

    private fun markFailed(trackFile: TrackFile) {
        trackFile.changeStatus(TrackFileStatus.FAILED)
        trackFileRepository.save(trackFile)
    }

}
