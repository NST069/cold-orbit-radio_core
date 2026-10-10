package com.coradio.notification.infrastructure.in.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScrobbleStreamPendingCleanerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @Mock
    private PendingMessage staleMessage;

    @Mock
    private PendingMessage freshMessage;

    @InjectMocks
    private ScrobbleStreamPendingCleaner cleaner;

    @BeforeEach
    void setUp() {
        RedisStreamProperties properties =
                new RedisStreamProperties(new RedisStreamProperties.Streams(
                        "scrobble-events",
                        ""
                ),
                        "notification-service",
                        "notification-consumer"
                );

        when(redisTemplate.opsForStream())
                .thenReturn(streamOperations);

        cleaner = new ScrobbleStreamPendingCleaner(
                redisTemplate,
                properties
        );
    }

    @Test
    void cleanup_shouldAcknowledgeAndDeleteStaleMessages() {
        RecordId messageId = RecordId.of("123-0");

        when(staleMessage.getId())
                .thenReturn(messageId);

        when(staleMessage.getIdAsString())
                .thenReturn("123-0");

        when(staleMessage.getElapsedTimeSinceLastDelivery())
                .thenReturn(Duration.ofHours(2));

        when(streamOperations.pending(
                "scrobble-events",
                "notification-service",
                Range.unbounded(),
                1000
        )).thenReturn(new PendingMessages("notification-service", List.of(staleMessage)));

        when(streamOperations.acknowledge(
                "scrobble-events",
                "notification-service",
                messageId
        )).thenReturn(1L);

        when(streamOperations.delete(
                "scrobble-events",
                messageId
        )).thenReturn(1L);

        cleaner.cleanup();

        verify(streamOperations).acknowledge(
                "scrobble-events",
                "notification-service",
                messageId
        );

        verify(streamOperations).delete(
                "scrobble-events",
                messageId
        );
    }

    @Test
    void cleanup_shouldIgnoreMessageYoungerThanMaxPendingIdle() {
        when(freshMessage.getElapsedTimeSinceLastDelivery())
                .thenReturn(Duration.ofMinutes(30));

        when(streamOperations.pending(
                "scrobble-events",
                "notification-service",
                Range.unbounded(),
                1000
        )).thenReturn(new PendingMessages("notification-service", List.of(freshMessage)));

        cleaner.cleanup();

        verify(streamOperations, never()).acknowledge(
                anyString(),
                anyString(),
                anyString()
        );

        verify(streamOperations, never()).delete(
                anyString(),
                anyString()
        );
    }

    @Test
    void cleanup_shouldProcessOnlyStaleMessages() {
        RecordId staleId = RecordId.of("123-0");
        RecordId freshId = RecordId.of("124-0");

        when(staleMessage.getId()).thenReturn(staleId);
        when(staleMessage.getElapsedTimeSinceLastDelivery())
                .thenReturn(Duration.ofHours(2));

        when(freshMessage.getElapsedTimeSinceLastDelivery())
                .thenReturn(Duration.ofMinutes(30));

        when(streamOperations.pending(
                "scrobble-events",
                "notification-service",
                Range.unbounded(),
                1000
        )).thenReturn(new PendingMessages("notification-service", List.of(staleMessage, freshMessage)));

        when(streamOperations.acknowledge(
                "scrobble-events",
                "notification-service",
                staleId
        )).thenReturn(1L);

        when(streamOperations.delete(
                "scrobble-events",
                staleId
        )).thenReturn(1L);

        cleaner.cleanup();

        verify(streamOperations).acknowledge(
                "scrobble-events",
                "notification-service",
                staleId
        );

        verify(streamOperations).delete(
                "scrobble-events",
                staleId
        );

        verify(streamOperations, never()).acknowledge(
                "scrobble-events",
                "notification-service",
                freshId
        );

        verify(streamOperations, never()).delete(
                "scrobble-events",
                freshId
        );
    }

    @Test
    void cleanup_shouldNotDeleteWhenAcknowledgeReturnsZero() {
        RecordId messageId = RecordId.of("123-0");

        when(staleMessage.getId())
                .thenReturn(messageId);

        when(staleMessage.getIdAsString())
                .thenReturn("123-0");

        when(staleMessage.getElapsedTimeSinceLastDelivery())
                .thenReturn(Duration.ofHours(2));

        when(streamOperations.pending(
                "scrobble-events",
                "notification-service",
                Range.unbounded(),
                1000
        )).thenReturn(new PendingMessages("notification-service", List.of(staleMessage)));

        when(streamOperations.acknowledge(
                "scrobble-events",
                "notification-service",
                messageId
        )).thenReturn(0L);

        cleaner.cleanup();

        verify(streamOperations).acknowledge(
                "scrobble-events",
                "notification-service",
                messageId
        );

        verify(streamOperations, never()).delete(
                anyString(),
                anyString()
        );
    }

    @Test
    void cleanup_shouldNotDeleteWhenAcknowledgeReturnsNull() {
        RecordId messageId = RecordId.of("123-0");

        when(staleMessage.getId())
                .thenReturn(messageId);

        when(staleMessage.getIdAsString())
                .thenReturn("123-0");

        when(staleMessage.getElapsedTimeSinceLastDelivery())
                .thenReturn(Duration.ofHours(2));

        when(streamOperations.pending(
                "scrobble-events",
                "notification-service",
                Range.unbounded(),
                1000
        )).thenReturn(new PendingMessages("notification-service", List.of(staleMessage)));

        when(streamOperations.acknowledge(
                "scrobble-events",
                "notification-service",
                messageId
        )).thenReturn(null);

        cleaner.cleanup();

        verify(streamOperations).acknowledge(
                "scrobble-events",
                "notification-service",
                messageId
        );

        verify(streamOperations, never()).delete(
                anyString(),
                anyString()
        );
    }

    @Test
    void cleanup_shouldDeleteOnlyAfterSuccessfulAcknowledge() {
        RecordId messageId = RecordId.of("123-0");

        when(staleMessage.getId())
                .thenReturn(messageId);

        when(staleMessage.getIdAsString())
                .thenReturn("123-0");

        when(staleMessage.getElapsedTimeSinceLastDelivery())
                .thenReturn(Duration.ofHours(2));

        when(streamOperations.pending(
                "scrobble-events",
                "notification-service",
                Range.unbounded(),
                1000
        )).thenReturn(new PendingMessages("notification-service", List.of(staleMessage)));

        when(streamOperations.acknowledge(
                "scrobble-events",
                "notification-service",
                messageId
        )).thenReturn(1L);

        when(streamOperations.delete(
                "scrobble-events",
                messageId
        )).thenReturn(1L);

        cleaner.cleanup();

        InOrder inOrder = inOrder(streamOperations);

        inOrder.verify(streamOperations).acknowledge(
                "scrobble-events",
                "notification-service",
                messageId
        );

        inOrder.verify(streamOperations).delete(
                "scrobble-events",
                messageId
        );
    }

    @Test
    void cleanup_shouldContinueWhenAcknowledgeFails() {
        RecordId failedId = RecordId.of("123-0");
        RecordId successfulId = RecordId.of("124-0");

        when(staleMessage.getId())
                .thenReturn(failedId);

        when(staleMessage.getIdAsString())
                .thenReturn("123-0");

        when(staleMessage.getElapsedTimeSinceLastDelivery())
                .thenReturn(Duration.ofHours(2));

        when(freshMessage.getId())
                .thenReturn(successfulId);

        when(freshMessage.getIdAsString())
                .thenReturn("124-0");

        when(freshMessage.getElapsedTimeSinceLastDelivery())
                .thenReturn(Duration.ofHours(2));

        when(streamOperations.pending(
                "scrobble-events",
                "notification-service",
                Range.unbounded(),
                1000
        )).thenReturn(new PendingMessages("notification-service", List.of(staleMessage, freshMessage)));

        when(streamOperations.acknowledge(
                "scrobble-events",
                "notification-service",
                failedId
        )).thenThrow(new RuntimeException("Redis unavailable"));

        when(streamOperations.acknowledge(
                "scrobble-events",
                "notification-service",
                successfulId
        )).thenReturn(1L);

        when(streamOperations.delete(
                "scrobble-events",
                successfulId
        )).thenReturn(1L);

        cleaner.cleanup();

        verify(streamOperations).acknowledge(
                "scrobble-events",
                "notification-service",
                failedId
        );

        verify(streamOperations, never()).delete(
                "scrobble-events",
                failedId
        );

        verify(streamOperations).acknowledge(
                "scrobble-events",
                "notification-service",
                successfulId
        );

        verify(streamOperations).delete(
                "scrobble-events",
                successfulId
        );
    }

    @Test
    void cleanup_shouldDoNothingWhenThereAreNoPendingMessages() {
        when(streamOperations.pending(
                "scrobble-events",
                "notification-service",
                Range.unbounded(),
                1000
        )).thenReturn(new PendingMessages("notification-service", List.of()));

        cleaner.cleanup();

        verify(streamOperations, never()).acknowledge(
                anyString(),
                anyString(),
                anyString()
        );

        verify(streamOperations, never()).delete(
                anyString(),
                anyString()
        );
    }
}
