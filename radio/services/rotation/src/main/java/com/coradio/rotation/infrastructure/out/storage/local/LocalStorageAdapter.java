package com.coradio.rotation.infrastructure.out.storage.local;

import com.coradio.rotation.application.dto.LocalFileInfo;
import com.coradio.rotation.application.exception.StorageException;
import com.coradio.rotation.domain.port.out.storage.LocalStoragePort;
import com.coradio.rotation.infrastructure.out.storage.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocalStorageAdapter implements LocalStoragePort {

    private final StorageProperties properties;

    @Override
    public void delete(Path localPath) {
        try {
            Files.deleteIfExists(localPath);
        } catch (IOException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    @Override
    public List<LocalFileInfo> listAllFiles() {
        try (Stream<Path> stream = Files.walk(Path.of(properties.localPath()))) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        try {
                            return new LocalFileInfo(
                                    path,
                                    Files.getLastModifiedTime(path).toInstant()
                            );
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .toList();
        } catch (IOException | UncheckedIOException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }
}
