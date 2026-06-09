package com.coradio.tgfetch

import io.minio.MinioClient
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
class TgfetchApplicationTest {

    @MockitoBean
    lateinit var minioClient: MinioClient

    @Test
    fun contextLoads() {
    }
}
