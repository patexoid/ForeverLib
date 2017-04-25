package com.patex.messaging;

import com.patex.LibException;
import com.patex.entities.ZUser;
import com.patex.service.ZUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Alexey on 19.03.2017.
 */
@Component
public class MessengerService {

    private List<Messenger> messengers = new ArrayList<>();

    @Autowired
    ZUserService userService;

    public void register(Messenger messenger) {
        messengers.add(messenger);
        toRole("ZLib Started", ZUserService.ADMIN_AUTHORITY, Collections.singletonList(messenger));
    }

    @PreDestroy()
    public void contextStoppedEvent() {
        toRole("Shutdown ZLib ", ZUserService.ADMIN_AUTHORITY);
    }


    public void sendMessageToUser(String message, ZUser user) throws LibException {
        if (user != null && user.getUserConfig() != null) {
            messengers.forEach(messenger -> messenger.sendToUser(message, user));
        }
    }

    public void toRole(String message, String role) {
        toRole(message, role, messengers);
    }

    private void toRole(String message, String role, Collection<Messenger> messengers) {
        Collection<ZUser> users = userService.getByRole(role);
        if (!users.isEmpty()) {
            messengers.forEach(messenger -> sendToUsers(messenger, message, users));
        }
    }

    private void sendToUsers(Messenger messenger, String mesage, Collection<ZUser> users) {
        users.forEach(user -> messenger.sendToUser(mesage, user));
    }

}
