package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.model.valueobject.StorageMissingSummary
import com.coradio.tgfetch.domain.model.valueobject.StorageOrphanSummary
import com.coradio.tgfetch.domain.port.`in`.StorageReconciliationUseCase
import com.coradio.tgfetch.domain.port.out.persistence.TrackFileRepositoryPort
import com.coradio.tgfetch.domain.port.out.storage.StorageGatewayPort
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class StorageReconciliationService  (
    val trackFileRepository: TrackFileRepositoryPort,
    val storageGateway: StorageGatewayPort
): StorageReconciliationUseCase {

    private val log = logger {}
    private lateinit var orphanSummary: StorageOrphanSummary
    private lateinit var missingSummary: StorageMissingSummary

    override fun removeOrphanedObjects() {
        log.info { "Storage reconciliation: orphan cleanup started" }
        val startedAt = Instant.now()

        orphanSummary = StorageOrphanSummary()
        val yesterday = Instant.now().minus(Duration.ofDays(1))

        val objects = storageGateway.listObjects()
        objects.forEach { obj ->
            orphanSummary.scanned++

            if (obj.lastModified > yesterday || trackFileRepository.existsByStorageKey(obj.storageKey)) return@forEach

            try {
                storageGateway.delete(obj.storageKey)
                orphanSummary.deleted++
                log.debug { "Deleted ${obj.storageKey}" }
            } catch (ex: Exception) {
                orphanSummary.failed++
                log.error(ex) { "Failed to delete orphaned ${obj.storageKey}" }
            }
        }

        val duration = Duration.between(
            startedAt,
            Instant.now()
        )
        log.info { "Storage reconciliation: orphan cleanup finished in ${duration.toSeconds()}s. Summary: $orphanSummary" }

    }

    override fun repairMissingObjects() {
        log.info { "Storage reconciliation: missing file repair started" }
        val startedAt = Instant.now()

        missingSummary = StorageMissingSummary()

        val storageKeys = trackFileRepository.findAllStorageKeys()
        storageKeys.forEach { key ->
            missingSummary.checked++

            try {
                if (storageGateway.exists(key)) return@forEach

                missingSummary.missing++
                trackFileRepository.markToRedownload(key)
                missingSummary.marked++
                log.debug { "Marked file for redownload: $key" }
            } catch (e: Exception) {
                missingSummary.failed++
                log.error(e) { "Failed to reconcile file $key" }
            }
        }

        val duration = Duration.between(
            startedAt,
            Instant.now()
        )
        log.info { "Storage reconciliation: missing file repair finished in ${duration.toSeconds()}s. Summary: $missingSummary" }
    }

}
