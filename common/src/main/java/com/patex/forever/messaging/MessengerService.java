package com.patex.forever.messaging;

import com.patex.forever.model.Res;
import com.patex.forever.model.User;
import com.patex.forever.service.Resources;
import com.patex.forever.service.UserService;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by Alexey on 19.03.2017.
 */
@Component
public class MessengerService {

    private final Resources res;
    private final UserService userService;

    private final List<Messenger> messengers = new ArrayList<>();

    public MessengerService(Resources res, UserService userService) {
        this.res = res;
        this.userService = userService;
    }

    public void register(Messenger messenger) {
        messengers.add(messenger);
        toRole(new Res("lib.started"), UserService.ADMIN_AUTHORITY, Collections.singletonList(messenger));
    }

    @PreDestroy
    public void stopEvent() {
        toRole(new Res("lib.stopped"), UserService.ADMIN_AUTHORITY);
    }

    public void sendMessageToUser(Res message, String username) {
        User user = userService.getUser(username);
        if (user != null && user.getUserConfig() != null) {
            Locale locale = user.getUserConfig().getLocale();
            String messageS = message.getMessage(res, locale);
            messengers.forEach(messenger -> messenger.sendToUser(messageS, user));
        }
    }

    public void toRole(Res message, String role) {
        toRole(message, role, messengers);
    }

    private void toRole(Res message, String role, Collection<Messenger> messengers) {
        Collection<User> users = userService.getByRole(role);
        messengers.forEach(messenger -> users.forEach(user -> sendMessageToUser(message, user.getUsername())));
    }
}
