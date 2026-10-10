package com.coradio.tgfetch.infrastructure.out.storage

import java.time.Instant

data class MinioObject(
    val storageKey: String,
    val lastModified: Instant
)
