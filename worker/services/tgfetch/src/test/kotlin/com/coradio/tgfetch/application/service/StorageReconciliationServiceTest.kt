package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.port.out.persistence.TrackFileRepositoryPort
import com.coradio.tgfetch.domain.port.out.storage.StorageGatewayPort
import com.coradio.tgfetch.infrastructure.out.storage.MinioObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Duration
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class StorageReconciliationServiceTest {

    @Mock
    private lateinit var trackFileRepository: TrackFileRepositoryPort

    @Mock
    private lateinit var storageGateway: StorageGatewayPort

    @InjectMocks
    private lateinit var service: StorageReconciliationService

    // removeOrphanedObjects

    @Test
    fun `should delete old object without database reference`() {
        val key = "music/orphan.mp3"
        val obj = minioObject(key, Duration.ofDays(2))

        Mockito.`when`(storageGateway.listObjects())
            .thenReturn(listOf(obj))
        Mockito.`when`(trackFileRepository.existsByStorageKey(key))
            .thenReturn(false)

        service.removeOrphanedObjects()

        Mockito.verify(storageGateway).delete(key)
    }

    @Test
    fun `should not delete recent object`() {
        val key = "music/recent.mp3"
        val obj = minioObject(key, Duration.ZERO)

        Mockito.`when`(storageGateway.listObjects())
            .thenReturn(listOf(obj))

        service.removeOrphanedObjects()

        Mockito.verify(storageGateway, Mockito.never()).delete(key)
        Mockito.verify(
            trackFileRepository,
            Mockito.never()
        ).existsByStorageKey(key)
    }

    @Test
    fun `should not delete object referenced in database`() {
        val key = "music/referenced.mp3"
        val obj = minioObject(key, Duration.ofDays(2))

        Mockito.`when`(storageGateway.listObjects())
            .thenReturn(listOf(obj))
        Mockito.`when`(trackFileRepository.existsByStorageKey(key))
            .thenReturn(true)

        service.removeOrphanedObjects()

        Mockito.verify(storageGateway, Mockito.never()).delete(key)
    }

    @Test
    fun `should continue deleting objects after deletion failure`() {
        val failedKey = "music/failed.mp3"
        val validKey = "music/orphan.mp3"

        Mockito.`when`(storageGateway.listObjects())
            .thenReturn(
                listOf(
                    minioObject(failedKey, Duration.ofDays(2)),
                    minioObject(validKey, Duration.ofDays(2))
                )
            )

        Mockito.`when`(
            trackFileRepository.existsByStorageKey(failedKey)
        ).thenReturn(false)

        Mockito.`when`(
            trackFileRepository.existsByStorageKey(validKey)
        ).thenReturn(false)

        Mockito.doThrow(RuntimeException("MinIO error"))
            .`when`(storageGateway)
            .delete(failedKey)

        service.removeOrphanedObjects()

        Mockito.verify(storageGateway).delete(failedKey)
        Mockito.verify(storageGateway).delete(validKey)
    }

    // repairMissingObjects

    @Test
    fun `should mark missing file for redownload`() {
        val key = "music/missing.mp3"

        Mockito.`when`(trackFileRepository.findAllStorageKeys())
            .thenReturn(listOf(key))
        Mockito.`when`(storageGateway.exists(key))
            .thenReturn(false)

        service.repairMissingObjects()

        Mockito.verify(trackFileRepository).markToRedownload(key)
    }

    @Test
    fun `should not mark existing file for redownload`() {
        val key = "music/existing.mp3"

        Mockito.`when`(trackFileRepository.findAllStorageKeys())
            .thenReturn(listOf(key))
        Mockito.`when`(storageGateway.exists(key))
            .thenReturn(true)

        service.repairMissingObjects()

        Mockito.verify(
            trackFileRepository,
            Mockito.never()
        ).markToRedownload(key)
    }

    @Test
    fun `should continue checking files after storage error`() {
        val failedKey = "music/error.mp3"
        val missingKey = "music/missing.mp3"

        Mockito.`when`(trackFileRepository.findAllStorageKeys())
            .thenReturn(listOf(failedKey, missingKey))

        Mockito.`when`(storageGateway.exists(failedKey))
            .thenThrow(RuntimeException("MinIO unavailable"))

        Mockito.`when`(storageGateway.exists(missingKey))
            .thenReturn(false)

        service.repairMissingObjects()

        Mockito.verify(
            trackFileRepository,
            Mockito.never()
        ).markToRedownload(failedKey)

        Mockito.verify(trackFileRepository).markToRedownload(missingKey)
    }

    @Test
    fun `should continue processing after database update failure`() {
        val failedKey = "music/db-error.mp3"
        val missingKey = "music/missing.mp3"

        Mockito.`when`(trackFileRepository.findAllStorageKeys())
            .thenReturn(listOf(failedKey, missingKey))

        Mockito.`when`(storageGateway.exists(failedKey))
            .thenReturn(false)

        Mockito.`when`(storageGateway.exists(missingKey))
            .thenReturn(false)

        Mockito.doThrow(RuntimeException("Database error"))
            .`when`(trackFileRepository)
            .markToRedownload(failedKey)

        service.repairMissingObjects()

        Mockito.verify(trackFileRepository).markToRedownload(failedKey)
        Mockito.verify(trackFileRepository).markToRedownload(missingKey)
    }

    private fun minioObject(
        key: String,
        age: Duration
    ) = MinioObject(
        storageKey = key,
        lastModified = Instant.now().minus(age)
    )
}
