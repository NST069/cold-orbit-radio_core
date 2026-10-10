package com.coradio.tgfetch.domain.model.valueobject

data class StorageMissingSummary(
    var checked: Int = 0,
    var missing: Int = 0,
    var marked: Int = 0,
    var failed: Int = 0
)
