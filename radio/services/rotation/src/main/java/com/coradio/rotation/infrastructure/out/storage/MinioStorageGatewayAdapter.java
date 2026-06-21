package com.coradio.rotation.infrastructure.out.storage;

import com.coradio.rotation.application.exception.FileDownloadingException;
import com.coradio.rotation.domain.port.out.storage.StorageGatewayPort;
import com.coradio.rotation.infrastructure.out.storage.config.StorageProperties;
import io.minio.DownloadObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioStorageGatewayAdapter implements StorageGatewayPort {

    private final MinioClient minioClient;

    private final StorageProperties properties;

    @Override
    public String downloadFile(String storageKey) {
        try {
            Path targetPath = Paths.get(
                    properties.localPath(),
                    storageKey
            );

            if (Files.exists(targetPath)) return targetPath.toString();

            Files.createDirectories(targetPath.getParent());

            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(storageKey)
                    .build();

            try (InputStream is = minioClient.getObject(
                    args
            )) {
                Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            log.debug("Downloaded file: {}", targetPath);

            return targetPath.toString();

        } catch (Exception ex) {
            log.error("Failed to download file {}", storageKey, ex);
            throw new FileDownloadingException(storageKey);
        }

    }
}
