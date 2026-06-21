package com.coradio.rotation.domain.port.out.storage;

public interface LocalStoragePort {

    void delete(String localPath);

    boolean exists(String localPath);
}
