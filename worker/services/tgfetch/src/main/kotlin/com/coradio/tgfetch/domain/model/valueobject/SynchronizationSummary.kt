package com.coradio.tgfetch.domain.model.valueobject

data class SynchronizationSummary(
    var created: Int = 0,
    var updated: Int = 0,
    var skipped: Int = 0,
    var failed: Int = 0
)