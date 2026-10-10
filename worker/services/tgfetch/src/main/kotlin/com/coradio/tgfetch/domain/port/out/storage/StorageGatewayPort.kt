package com.coradio.tgfetch.domain.port.out.storage

import com.coradio.tgfetch.infrastructure.out.storage.MinioObject
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

    fun listObjects(): List<MinioObject>

}
