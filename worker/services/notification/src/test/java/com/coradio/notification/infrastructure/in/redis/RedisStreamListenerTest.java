package com.coradio.notification.infrastructure.in.redis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisStreamListenerTest {

    @Mock
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @Mock
    private RedisStreamHandler handler;

    @Mock
    private MapRecord<String, String, String> message;

    @InjectMocks
    private RedisStreamListener listener;

    @Test
    void listen_shouldRegisterMessageListener() {
        String stream = "test-stream";
        String consumerGroup = "test-group";
        String consumerName = "test-consumer";

        listener.listen(
                stream,
                consumerGroup,
                consumerName,
                handler
        );

        verify(container).receive(
                eq(Consumer.from(consumerGroup, consumerName)),
                eq(StreamOffset.create(stream, ReadOffset.lastConsumed())),
                any()
        );
    }

    @Test
    void listen_shouldHandleMessageAndAcknowledgeAndDeleteOnSuccess() {
        String stream = "test-stream";
        String consumerGroup = "test-group";
        String consumerName = "test-consumer";

        RecordId recordId = RecordId.of("123-0");

        when(message.getId()).thenReturn(recordId);
        when(handler.handle(message)).thenReturn(true);
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);

        ArgumentCaptor<StreamListener<String, MapRecord<String, String, String>>> captor =
                ArgumentCaptor.forClass(StreamListener.class);

        listener.listen(
                stream,
                consumerGroup,
                consumerName,
                handler
        );

        verify(container).receive(
                eq(Consumer.from(consumerGroup, consumerName)),
                eq(StreamOffset.create(stream, ReadOffset.lastConsumed())),
                captor.capture()
        );

        StreamListener<String, MapRecord<String, String, String>> streamListener =
                captor.getValue();

        streamListener.onMessage(message);

        verify(handler).handle(message);
        verify(streamOperations).acknowledge(
                stream,
                consumerGroup,
                recordId
        );
        verify(streamOperations).delete(
                stream,
                recordId
        );
    }

    @Test
    void listen_shouldNotAcknowledgeOrDeleteWhenHandlerFails() {
        String stream = "test-stream";
        String consumerGroup = "test-group";
        String consumerName = "test-consumer";

        RecordId recordId = RecordId.of("123-0");

        RuntimeException exception = new RuntimeException("handler failed");

        when(message.getId()).thenReturn(recordId);
        when(handler.handle(message)).thenThrow(exception);

        ArgumentCaptor<StreamListener<String, MapRecord<String, String, String>>> captor =
                ArgumentCaptor.forClass(StreamListener.class);

        listener.listen(
                stream,
                consumerGroup,
                consumerName,
                handler
        );

        verify(container).receive(
                eq(Consumer.from(consumerGroup, consumerName)),
                eq(StreamOffset.create(stream, ReadOffset.lastConsumed())),
                captor.capture()
        );

        captor.getValue().onMessage(message);

        verify(handler).handle(message);

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
    void listen_shouldAcknowledgeAndDeleteWhenHandlerReturnsFalse() {
        String stream = "test-stream";
        String consumerGroup = "test-group";
        String consumerName = "test-consumer";

        RecordId recordId = RecordId.of("123-0");

        when(message.getId()).thenReturn(recordId);
        when(handler.handle(message)).thenReturn(false);
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);

        ArgumentCaptor<StreamListener<String, MapRecord<String, String, String>>> captor =
                ArgumentCaptor.forClass(StreamListener.class);

        listener.listen(
                stream,
                consumerGroup,
                consumerName,
                handler
        );

        verify(container).receive(
                eq(Consumer.from(consumerGroup, consumerName)),
                eq(StreamOffset.create(stream, ReadOffset.lastConsumed())),
                captor.capture()
        );

        captor.getValue().onMessage(message);

        verify(handler).handle(message);
        verify(streamOperations).acknowledge(
                stream,
                consumerGroup,
                recordId
        );
        verify(streamOperations).delete(
                stream,
                recordId
        );
    }

    @Test
    void start_shouldStartContainer() {
        listener.start();

        verify(container).start();
    }
}
