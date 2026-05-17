package com.coradio.tgfetch.domain.model

import java.util.UUID

data class Track (
    val id : UUID?=null,
    val title : String,
    val artist: String,
    val duration: Int
)
