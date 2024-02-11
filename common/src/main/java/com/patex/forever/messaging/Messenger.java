package com.patex.forever.messaging;

import com.patex.forever.model.User;

/**
 * Created by Alexey on 24.04.2017.
 */
public interface Messenger {

    void sendToUser(String message, User user);
}
