package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.enums.TrackFileStatus
import com.coradio.tgfetch.domain.model.mapper.ExtensionMapper
import com.coradio.tgfetch.domain.model.valueobject.DownloadSummary
import com.coradio.tgfetch.domain.model.view.TrackFileJobView
import com.coradio.tgfetch.domain.port.`in`.DownloadPendingTrackFilesUseCase
import com.coradio.tgfetch.domain.port.out.persistence.TrackFileRepositoryPort
import com.coradio.tgfetch.domain.port.out.storage.AudioMetadataServicePort
import com.coradio.tgfetch.domain.port.out.storage.StorageGatewayPort
import com.coradio.tgfetch.domain.port.out.telegram.TelegramGatewayPort
import com.coradio.tgfetch.infrastructure.exception.InfrastructureException
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
class DownloadPendingTrackFilesService(
    private val trackFileRepository: TrackFileRepositoryPort,
    private val telegramGateway: TelegramGatewayPort,
    private val storageGateway: StorageGatewayPort,
    private val audioMetadataService: AudioMetadataServicePort,
): DownloadPendingTrackFilesUseCase {

    private val log = logger {}
    private lateinit var summary: DownloadSummary

    override fun execute(): DownloadSummary {
        log.info { "Starting download of pending track files" }
        val startedAt = Instant.now()

        summary = DownloadSummary()

        markPending();

        val pendingFiles = trackFileRepository.findAllByStatus(TrackFileStatus.PENDING)

        pendingFiles.forEach { trackFile ->
            trackFile.id
                ?.let {
                    try {
                        if (!markDownloading(trackFile.id)) {
                            log.info { "Track ${trackFile.id} already picked by another worker" }
                            return@forEach
                        }
                        val file = telegramGateway.downloadFile(trackFile.telegramFileId, resolveExtension(trackFile))
                        val storageKey = generateStorageKey(trackFile.id, file)
                        audioMetadataService.rewriteMetadata(file, trackFile.artist, trackFile.title)
                        if (storageGateway.exists(storageKey)) {
                            storageGateway.delete(storageKey)
                        }
                        storageGateway.upload(storageKey, file)
                        markReady(trackFile.id, storageKey)
                        Files.deleteIfExists(file)
                        log.debug { "Downloaded ${trackFile.artist} by ${trackFile.title}" }
                        summary.success++
                    } catch (ex: InfrastructureException) {
                        markFailed(trackFile.id)
                        log.warn(ex) { "Failed to download ${trackFile.artist} - ${trackFile.title}" }
                        summary.failed++
                    }
                }
        }

        val duration = Duration.between(
            startedAt,
            Instant.now()
        )
        log.info { "Download completed in ${duration.toSeconds()}s. Summary: $summary" }

        return summary
    }

    private fun generateStorageKey(id: UUID?, file: Path): String {
        return "tracks/${id ?: UUID.randomUUID()}.${file.fileName.toString().substringAfterLast('.', "")}"
    }

    private fun markPending() {
        val createdFiles = trackFileRepository.findAllByStatus(TrackFileStatus.CREATED)
        createdFiles.forEach { trackFile ->
            trackFile.id
                ?.let {
                    trackFileRepository.updateStatus(trackFile.id, TrackFileStatus.PENDING, TrackFileStatus.CREATED)
                }
        }
    }

    private fun markDownloading(id: UUID): Boolean {
        return trackFileRepository.updateStatus(id, TrackFileStatus.DOWNLOADING, TrackFileStatus.PENDING) == 1
    }

    private fun markFailed(id: UUID) {
        trackFileRepository.updateStatus(id, TrackFileStatus.FAILED, TrackFileStatus.DOWNLOADING)
    }

    private fun markReady(id: UUID, storageKey: String) {
        trackFileRepository.markReady(id, storageKey)
    }

    private fun resolveExtension(file: TrackFileJobView): String? {
        return file.mimeType?.let(ExtensionMapper::mimeToExt)
            ?: file.fileName?.substringAfterLast('.', "")
    }

}
