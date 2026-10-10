package com.coradio.notification.infrastructure.in.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamListener;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisStreamInitializerTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @Mock
    private RedisStreamProperties properties;

    @Mock
    private RedisStreamHandler streamHandler;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @InjectMocks
    private RedisStreamInitializer initializer;

    @BeforeEach
    void setUp() {
        when(streamHandler.getStream()).thenReturn("test-stream");

        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
    }

    @Test
    void initialize_shouldCreateGroupSubscribeAndStartContainer() {
        when(properties.consumerName()).thenReturn("test-consumer");
        when(properties.consumerGroup()).thenReturn("test-group");

        initializer.initialize();

        verify(streamOperations).createGroup(
                "test-stream",
                ReadOffset.from("0-0"),
                "test-group"
        );

        verify(container).receive(
                eq(Consumer.from("test-group", "test-consumer")),
                eq(StreamOffset.create(
                        "test-stream",
                        ReadOffset.lastConsumed()
                )),
                any()
        );

        verify(container).start();
    }

    @Test
    void initialize_shouldIgnoreGroupAlreadyExistsException() {
        when(properties.consumerName()).thenReturn("test-consumer");
        when(properties.consumerGroup()).thenReturn("test-group");

        RuntimeException exception = new RuntimeException(
                "BUSYGROUP Consumer Group name already exists"
        );

        doThrow(exception)
                .when(streamOperations)
                .createGroup(
                        "test-stream",
                        ReadOffset.from("0-0"),
                        "test-group"
                );

        initializer.initialize();

        verify(streamOperations).createGroup(
                "test-stream",
                ReadOffset.from("0-0"),
                "test-group"
        );

        verify(container).receive(
                eq(Consumer.from("test-group", "test-consumer")),
                eq(StreamOffset.create(
                        "test-stream",
                        ReadOffset.lastConsumed()
                )),
                any()
        );

        verify(container).start();
    }

    @Test
    void initialize_shouldIgnoreGroupAlreadyExistsExceptionFromCause() {
        when(properties.consumerName()).thenReturn("test-consumer");
        when(properties.consumerGroup()).thenReturn("test-group");

        RuntimeException exception = new RuntimeException(
                "Redis operation failed",
                new IllegalStateException("BUSYGROUP Consumer Group already exists")
        );

        doThrow(exception)
                .when(streamOperations)
                .createGroup(
                        "test-stream",
                        ReadOffset.from("0-0"),
                        "test-group"
                );

        initializer.initialize();

        verify(container).receive(
                eq(Consumer.from("test-group", "test-consumer")),
                eq(StreamOffset.create(
                        "test-stream",
                        ReadOffset.lastConsumed()
                )),
                any()
        );

        verify(container).start();
    }

    @Test
    void initialize_shouldRethrowUnexpectedException() {
        when(properties.consumerGroup()).thenReturn("test-group");

        RuntimeException exception = new RuntimeException(
                "Redis connection failed"
        );

        doThrow(exception)
                .when(streamOperations)
                .createGroup(
                        "test-stream",
                        ReadOffset.from("0-0"),
                        "test-group"
                );

        assertThatThrownBy(() -> initializer.initialize())
                .isSameAs(exception);

        verify(container, never()).receive(
                any(),
                any(),
                any()
        );

        verify(container, never()).start();
    }

    @Test
    void initialize_shouldUseStreamHandlerAsListener() {
        when(properties.consumerName()).thenReturn("test-consumer");
        when(properties.consumerGroup()).thenReturn("test-group");

        ArgumentCaptor<StreamListener<String, MapRecord<String, String, String>>> listenerCaptor =
                ArgumentCaptor.forClass(StreamListener.class);

        initializer.initialize();

        verify(container).receive(
                eq(Consumer.from("test-group", "test-consumer")),
                eq(StreamOffset.create(
                        "test-stream",
                        ReadOffset.lastConsumed()
                )),
                listenerCaptor.capture()
        );

        MapRecord<String, String, String> message = mock(MapRecord.class);

        listenerCaptor.getValue().onMessage(message);

        verify(streamHandler).handle(message);
    }

    @Test
    void initialize_shouldUseConfiguredConsumerGroup() {
        when(properties.consumerName()).thenReturn("test-consumer");
        when(properties.consumerGroup()).thenReturn("custom-group");

        initializer.initialize();

        verify(streamOperations).createGroup(
                "test-stream",
                ReadOffset.from("0-0"),
                "custom-group"
        );

        verify(container).receive(
                eq(Consumer.from("custom-group", "test-consumer")),
                eq(StreamOffset.create(
                        "test-stream",
                        ReadOffset.lastConsumed()
                )),
                any()
        );
    }

    @Test
    void initialize_shouldUseConfiguredConsumerName() {
        when(properties.consumerGroup()).thenReturn("test-group");
        when(properties.consumerName()).thenReturn("custom-consumer");

        initializer.initialize();

        verify(container).receive(
                eq(Consumer.from("test-group", "custom-consumer")),
                eq(StreamOffset.create(
                        "test-stream",
                        ReadOffset.lastConsumed()
                )),
                any()
        );
    }
}
