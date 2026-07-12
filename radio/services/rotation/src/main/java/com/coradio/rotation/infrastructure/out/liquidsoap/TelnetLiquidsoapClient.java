package com.coradio.rotation.infrastructure.out.liquidsoap;

import com.coradio.rotation.infrastructure.exception.PlaybackEngineException;
import com.coradio.rotation.infrastructure.out.liquidsoap.config.LiquidsoapProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelnetLiquidsoapClient implements LiquidsoapClient {

    private final LiquidsoapProperties properties;

    @Override
    public String execute(String command) {

        try (
                Socket socket = new Socket(properties.host(), properties.port());
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );
                PrintWriter writer = new PrintWriter(
                        socket.getOutputStream(), true
                )
        ) {
            socket.setSoTimeout(properties.timeout());

            writer.println(command.trim());

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if ("END".equals(line)) break;
                response.append(line).append('\n');
            }

            String result = response.toString().trim();

            log.trace("Liquidsoap command '{}': '{}'", command, result);

            return result;

        } catch (IOException ex) {
            throw new PlaybackEngineException("Failed to execute command: " + command, ex);
        }
    }

    @Override
    public boolean isAvailable() {

        try (Socket socket = new Socket()) {
            socket.connect(
                    new InetSocketAddress(
                            properties.host(),
                            properties.port()
                    ),
                    properties.timeout()
            );
            return true;
        } catch (IOException ex) {
            log.debug("Liquidsoap is unavailable", ex);
            return false;
        }
    }

}
