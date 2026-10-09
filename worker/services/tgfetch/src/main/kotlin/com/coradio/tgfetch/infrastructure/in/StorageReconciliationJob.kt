package com.coradio.tgfetch.infrastructure.`in`

import com.coradio.tgfetch.domain.port.`in`.StorageReconciliationUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class StorageReconciliationJob(
    private val reconciliationService: StorageReconciliationUseCase
) {
    @Scheduled(cron = "\${jobs.clean-storage-cron}")
    fun reconcileStorage() {
        reconciliationService.removeOrphanedObjects()
        reconciliationService.repairMissingObjects()
    }
}
