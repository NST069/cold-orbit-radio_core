package com.coradio.tgfetch.domain.port.out.storage

import java.nio.file.Path

interface StorageGatewayPort {

    fun upload(
        key: String,
        file: Path
    )

    fun exists(
        key: String
    ): Boolean

    fun delete(
        key: String
    )
}
