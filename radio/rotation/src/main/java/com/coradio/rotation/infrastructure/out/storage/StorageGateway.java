package com.coradio.rotation.infrastructure.out.storage;

import com.coradio.rotation.domain.port.out.storage.StorageGatewayPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StorageGateway implements StorageGatewayPort {
    @Override
    public String downloadFile(String s) {
        return "";
    }
}
