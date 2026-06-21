package com.coradio.rotation.infrastructure.out.storage;

import com.coradio.rotation.application.exception.FileDownloadingException;
import com.coradio.rotation.infrastructure.out.storage.config.StorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class MinioStorageGatewayAdapterIntegrationTest {

    @Container
    static GenericContainer<?> minio =
            new GenericContainer<>("minio/minio:latest")
                    .withExposedPorts(9000)
                    .withEnv("MINIO_ROOT_USER", "minioadmin")
                    .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
                    .withCommand("server /data");

    @TempDir
    Path tempDir;

    private MinioClient minioClient;

    private MinioStorageGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        String endpoint = String.format(
                "http://%s:%d",
                minio.getHost(),
                minio.getMappedPort(9000)
        );

        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials("minioadmin", "minioadmin")
                .build();

        StorageProperties properties = new StorageProperties(
                endpoint,
                "minioadmin",
                "minioadmin",
                "test-bucket",
                tempDir.toString()
        );

        adapter = new MinioStorageGatewayAdapter(minioClient, properties);
    }

    @BeforeEach
    void createBucket() throws Exception {

        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket("test-bucket")
                        .build()
        );

        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket("test-bucket")
                            .build()
            );
        }
    }

    @Test
    void shouldDownloadFile() throws Exception {

        String storageKey = "tracks/test.mp3";

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("test-bucket")
                        .object(storageKey)
                        .stream(
                                new ByteArrayInputStream("hello".getBytes()),
                                5,
                                -1
                        )
                        .build()
        );

        String path = adapter.downloadFile(storageKey);

        Path file = Path.of(path);

        assertTrue(Files.exists(file));
        assertEquals("hello", Files.readString(file));
    }

    @Test
    void shouldNotDownloadExistingFile() throws Exception {

        String storageKey = "tracks/test.mp3";

        Path file = tempDir.resolve(storageKey);

        Files.createDirectories(file.getParent());
        Files.writeString(file, "cached");

        String path = adapter.downloadFile(storageKey);

        assertEquals("cached", Files.readString(Path.of(path)));
    }

    @Test
    void shouldThrowExceptionWhenObjectNotFound() {

        assertThrows(
                FileDownloadingException.class,
                () -> adapter.downloadFile("tracks/missing.mp3")
        );
    }

}
