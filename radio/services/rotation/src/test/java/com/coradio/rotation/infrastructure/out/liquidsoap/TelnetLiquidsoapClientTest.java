package com.coradio.rotation.infrastructure.out.liquidsoap;

import com.coradio.rotation.application.exception.PlaybackEngineException;
import com.coradio.rotation.infrastructure.out.liquidsoap.config.LiquidsoapProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class TelnetLiquidsoapClientTest {

    private ServerSocket server;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        executor = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (server != null && !server.isClosed()) {
            server.close();
        }
        executor.shutdownNow();
    }

    @Test
    void executeShouldReturnResponse() throws Exception {

        server = new ServerSocket(0);

        executor.submit(() -> {
            try (
                    Socket socket = server.accept();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(socket.getInputStream())
                    );
                    PrintWriter writer = new PrintWriter(
                            socket.getOutputStream(),
                            true
                    )
            ) {
                assertEquals("coldorbit.length", reader.readLine());

                writer.println("5");
                writer.println("END");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        TelnetLiquidsoapClient client = new TelnetLiquidsoapClient(
                new LiquidsoapProperties(
                        "localhost",
                        server.getLocalPort(),
                        1000
                )
        );

        String result = client.execute("coldorbit.length");

        assertEquals("5", result);
    }

    @Test
    void executeShouldReturnMultilineResponse() throws Exception {

        server = new ServerSocket(0);

        executor.submit(() -> {
            try (
                    Socket socket = server.accept();
                    PrintWriter writer = new PrintWriter(
                            socket.getOutputStream(),
                            true
                    )
            ) {
                writer.println("line1");
                writer.println("line2");
                writer.println("END");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        TelnetLiquidsoapClient client = new TelnetLiquidsoapClient(
                new LiquidsoapProperties(
                        "localhost",
                        server.getLocalPort(),
                        1000
                )
        );

        String result = client.execute("test");

        assertEquals("line1\nline2", result);
    }

    @Test
    void executeShouldTrimCommand() throws Exception {

        server = new ServerSocket(0);

        executor.submit(() -> {
            try (
                    Socket socket = server.accept();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(socket.getInputStream())
                    );
                    PrintWriter writer = new PrintWriter(
                            socket.getOutputStream(),
                            true
                    )
            ) {

                assertEquals(
                        "coldorbit.length",
                        reader.readLine()
                );

                writer.println("1");
                writer.println("END");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        TelnetLiquidsoapClient client = new TelnetLiquidsoapClient(
                new LiquidsoapProperties(
                        "localhost",
                        server.getLocalPort(),
                        1000
                )
        );

        client.execute("  coldorbit.length  ");
    }

    @Test
    void executeShouldThrowPlaybackEngineExceptionWhenConnectionFails() {

        TelnetLiquidsoapClient client = new TelnetLiquidsoapClient(
                new LiquidsoapProperties(
                        "localhost",
                        65535,
                        100
                )
        );

        assertThrows(
                PlaybackEngineException.class,
                () -> client.execute("test")
        );
    }

    @Test
    void isAvailableShouldReturnTrue() throws Exception {

        server = new ServerSocket(0);

        executor.submit(() -> {
            try (Socket ignored = server.accept()) {
                Thread.sleep(100);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        TelnetLiquidsoapClient client = new TelnetLiquidsoapClient(
                new LiquidsoapProperties(
                        "localhost",
                        server.getLocalPort(),
                        1000
                )
        );

        assertTrue(client.isAvailable());
    }

    @Test
    void isAvailableShouldReturnFalse() {

        TelnetLiquidsoapClient client = new TelnetLiquidsoapClient(
                new LiquidsoapProperties(
                        "localhost",
                        65535,
                        100
                )
        );

        assertFalse(client.isAvailable());
    }
}
