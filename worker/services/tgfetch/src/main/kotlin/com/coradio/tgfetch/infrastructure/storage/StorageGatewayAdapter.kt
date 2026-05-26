package com.coradio.tgfetch.infrastructure.storage

import com.coradio.tgfetch.domain.port.out.storage.StorageGatewayPort
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class StorageGatewayAdapter: StorageGatewayPort {
    override suspend fun upload(key: String, stream: InputStream) {
        TODO("Not yet implemented")
    }

    override suspend fun exists(key: String): Boolean {
        TODO("Not yet implemented")
    }
}
