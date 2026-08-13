package com.coradio.rotation.infrastructure.out.liquidsoap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiquidsoapTelnetAdapterTest {

    @Mock
    private LiquidsoapClient client;

    @InjectMocks
    private LiquidsoapTelnetAdapter adapter;

    @Test
    void getQueueLength_shouldReturnQueueLength() {
        when(client.execute("coldorbit.length"))
                .thenReturn("5");

        int length = adapter.getQueueLength();

        assertEquals(5, length);
    }

    @Test
    void getQueueLength_shouldTrimQueueLengthResponse() {
        when(client.execute("coldorbit.length"))
                .thenReturn(" 5 \n");

        int length = adapter.getQueueLength();

        assertEquals(5, length);
    }

    @Test
    void getQueueLength_shouldThrowNumberFormatExceptionWhenQueueLengthIsInvalid() {
        when(client.execute("coldorbit.length"))
                .thenReturn("abc");

        assertThrows(
                NumberFormatException.class,
                () -> adapter.getQueueLength()
        );
    }

    @Test
    void getCurrentTrack_shouldReturnCurrentTrack() {
        when(client.execute("coldorbit.current"))
                .thenReturn("track-1");

        Optional<String> track = adapter.getCurrentTrack();

        assertTrue(track.isPresent());
        assertEquals("track-1", track.get());
    }

    @Test
    void getCurrentTrack_shouldReturnEmptyWhenCurrentTrackIsEmpty() {
        when(client.execute("coldorbit.current"))
                .thenReturn("");

        Optional<String> track = adapter.getCurrentTrack();

        assertTrue(track.isEmpty());
    }

    @Test
    void getCurrentTrack_shouldReturnEmptyWhenCurrentTrackContainsOnlyWhitespace() {
        when(client.execute("coldorbit.current"))
                .thenReturn(" \n ");

        Optional<String> track = adapter.getCurrentTrack();

        assertTrue(track.isEmpty());
    }

    @Test
    void enqueue_shouldNormalizeWindowsPath() {
        adapter.enqueue("C:\\music\\tracks\\test.mp3");

        verify(client)
                .execute("coldorbit.push C:/music/tracks/test.mp3");
    }

    @Test
    void enqueue_shouldKeepUnixPathUntouched() {
        adapter.enqueue("/music/tracks/test.mp3");

        verify(client)
                .execute("coldorbit.push /music/tracks/test.mp3");
    }
}
