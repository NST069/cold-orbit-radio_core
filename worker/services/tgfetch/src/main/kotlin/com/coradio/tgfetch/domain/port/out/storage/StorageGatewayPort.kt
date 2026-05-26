package com.coradio.tgfetch.domain.port.out.storage

import java.io.InputStream

interface StorageGatewayPort {

    suspend fun upload(
        key: String,
        stream: InputStream
    )

    suspend fun exists(
        key: String
    ): Boolean
}
