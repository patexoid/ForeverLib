package com.patex.messaging;

import java.util.stream.Stream;

public interface MessengerListener {

    Stream<String> createResponse(String request);

    default boolean requireUserAuth() {
        return true;
    }
}
