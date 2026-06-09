package com.coradio.tgfetch.application.service

import com.coradio.tgfetch.domain.enums.TrackFileStatus
import com.coradio.tgfetch.domain.model.view.TrackFileJobView
import com.coradio.tgfetch.domain.port.out.persistence.TrackFileRepositoryPort
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class RetryFailedTrackFilesServiceTest {

    @Mock
    lateinit var trackFileRepository: TrackFileRepositoryPort

    @InjectMocks
    lateinit var retryFailedTrackFilesService: RetryFailedTrackFilesService

    private fun track(
        id: UUID? = UUID.randomUUID(),
        retryCount: Int = 0,
    ) = TrackFileJobView(
        id = id,
        telegramFileId = "telegram-id",
        artist = "Artist",
        title = "Title",
        retryCount = retryCount,
    )

    @Test
    fun `should retry failed track when retry count is less than or equal to five`() {
        val id = UUID.randomUUID()

        whenever(
            trackFileRepository.findAllByStatus(TrackFileStatus.FAILED)
        ).thenReturn(
            listOf(
                track(
                    id = id,
                    retryCount = 5,
                )
            )
        )

        whenever(
            trackFileRepository.findAllByStatus(TrackFileStatus.CREATED)
        ).thenReturn(emptyList())

        retryFailedTrackFilesService.execute()

        verify(trackFileRepository)
            .incrementRetry(id)

        verify(trackFileRepository)
            .updateStatus(
                id,
                TrackFileStatus.PENDING,
                TrackFileStatus.FAILED
            )

        verify(trackFileRepository, never())
            .updateStatus(
                id,
                TrackFileStatus.FAILED_PERMANENTLY,
                TrackFileStatus.FAILED
            )
    }

    @Test
    fun `should mark failed permanently when retry count exceeds five`() {
        val id = UUID.randomUUID()

        whenever(
            trackFileRepository.findAllByStatus(TrackFileStatus.FAILED)
        ).thenReturn(
            listOf(
                track(
                    id = id,
                    retryCount = 6,
                )
            )
        )

        whenever(
            trackFileRepository.findAllByStatus(TrackFileStatus.CREATED)
        ).thenReturn(emptyList())

        retryFailedTrackFilesService.execute()

        verify(trackFileRepository, never())
            .incrementRetry(any())

        verify(trackFileRepository)
            .updateStatus(
                id,
                TrackFileStatus.FAILED_PERMANENTLY,
                TrackFileStatus.FAILED
            )
    }

    @Test
    fun `should move created tracks to pending`() {
        val id = UUID.randomUUID()

        whenever(
            trackFileRepository.findAllByStatus(TrackFileStatus.FAILED)
        ).thenReturn(emptyList())

        whenever(
            trackFileRepository.findAllByStatus(TrackFileStatus.CREATED)
        ).thenReturn(
            listOf(
                track(
                    id = id,
                )
            )
        )

        retryFailedTrackFilesService.execute()

        verify(trackFileRepository)
            .updateStatus(
                id,
                TrackFileStatus.PENDING,
                TrackFileStatus.CREATED
            )
    }

    @Test
    fun `should process failed and created tracks`() {
        val failedId = UUID.randomUUID()
        val createdId = UUID.randomUUID()

        whenever(
            trackFileRepository.findAllByStatus(TrackFileStatus.FAILED)
        ).thenReturn(
            listOf(
                track(
                    id = failedId,
                    retryCount = 2,
                )
            )
        )

        whenever(
            trackFileRepository.findAllByStatus(TrackFileStatus.CREATED)
        ).thenReturn(
            listOf(
                track(
                    id = createdId,
                )
            )
        )

        retryFailedTrackFilesService.execute()

        verify(trackFileRepository)
            .incrementRetry(failedId)

        verify(trackFileRepository)
            .updateStatus(
                failedId,
                TrackFileStatus.PENDING,
                TrackFileStatus.FAILED
            )

        verify(trackFileRepository)
            .updateStatus(
                createdId,
                TrackFileStatus.PENDING,
                TrackFileStatus.CREATED
            )
    }

    @Test
    fun `should ignore tracks without id`() {
        whenever(
            trackFileRepository.findAllByStatus(TrackFileStatus.FAILED)
        ).thenReturn(
            listOf(
                track(
                    id = null,
                    retryCount = 2,
                )
            )
        )

        whenever(
            trackFileRepository.findAllByStatus(TrackFileStatus.CREATED)
        ).thenReturn(emptyList())

        retryFailedTrackFilesService.execute()

        verify(trackFileRepository, never())
            .incrementRetry(any())

        verify(trackFileRepository, never())
            .updateStatus(
                any(),
                any(),
                any()
            )
    }
}
