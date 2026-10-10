package com.coradio.tgfetch.domain.model.valueobject

data class StorageOrphanSummary(
    var scanned: Int = 0,
    var deleted: Int = 0,
    var failed: Int = 0
)
