package com.coradio.rotation.domain.port.out.storage;

import com.coradio.rotation.application.dto.LocalFileInfo;
import java.nio.file.Path;
import java.util.List;

public interface LocalStoragePort {

    void delete(Path localPath);
    List<LocalFileInfo> listAllFiles();
}
