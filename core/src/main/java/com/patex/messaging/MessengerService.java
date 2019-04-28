package com.patex.messaging;

import com.patex.entities.ZUser;
import com.patex.service.Resources;
import com.patex.service.ZUserService;
import com.patex.utils.Res;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
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
    private final ZUserService userService;

    private final List<Messenger> messengers = new ArrayList<>();

    public MessengerService(Resources res, ZUserService userService) {
        this.res = res;
        this.userService = userService;
    }

    public void register(Messenger messenger) {
        messengers.add(messenger);
        toRole(new Res("lib.started"), ZUserService.ADMIN_AUTHORITY, Collections.singletonList(messenger));
    }

    @PreDestroy()
    public void stopEvent() {
        toRole(new Res("lib.stopped"), ZUserService.ADMIN_AUTHORITY);
    }

    public void sendMessageToUser(Res message, String username) {
       sendMessageToUser(message,userService.loadUserByUsername(username));
    }

    public void sendMessageToUser(Res message, ZUser user) {
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
        Collection<ZUser> users = userService.getByRole(role);
        messengers.forEach(messenger -> users.forEach(user -> sendMessageToUser(message, user)));
    }
}
