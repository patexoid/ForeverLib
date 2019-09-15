package com.patex.messaging;

import com.patex.model.User;

/**
 * Created by Alexey on 24.04.2017.
 */
public interface Messenger {

    void sendToUser(String message, User user);
}
