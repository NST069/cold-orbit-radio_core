package com.coradio.rotation.infrastructure.out.liquidsoap;

public interface LiquidsoapClient {

    String execute(String command);

    boolean isAvailable();
}
