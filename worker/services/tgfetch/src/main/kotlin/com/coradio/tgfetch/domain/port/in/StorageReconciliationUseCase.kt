package com.coradio.tgfetch.domain.port.`in`

interface StorageReconciliationUseCase {
    fun removeOrphanedObjects()

    fun repairMissingObjects()
}
