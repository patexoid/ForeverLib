package com.patex.messaging;

import com.patex.entities.ZUser;
import com.patex.entities.ZUserConfig;
import com.patex.service.Resources;
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

    @Autowired
    Resources res;
    private final List<Messenger> messengers = new ArrayList<>();
    @Autowired
    private ZUserService userService;

    public void register(Messenger messenger) {
        messengers.add(messenger);
        toRole(new ZMessage("lib.started"), ZUserService.ADMIN_AUTHORITY, Collections.singletonList(messenger));
    }

    @PreDestroy()
    public void contextStoppedEvent() {
        toRole(new ZMessage("lib.stopped"), ZUserService.ADMIN_AUTHORITY);
    }


    public void sendMessageToUser(ZUser user, String message, Object... objs) {
        sendMessageToUser(new ZMessage(message, objs), user);
    }


    public void sendMessageToUser(ZMessage message, ZUser user) {
        if (user != null && user.getUserConfig() != null) {
            messengers.forEach(messenger ->
                    messenger.sendToUser(message.getMessage(res, user.getUserConfig().getLocale()), user));
        }
    }

    public void toRole(ZMessage message, String role) {
        toRole(message, role, messengers);
    }

    private void toRole(ZMessage message, String role, Collection<Messenger> messengers) {
        Collection<ZUser> users = userService.getByRole(role);
        if (!users.isEmpty()) {
            messengers.forEach(messenger -> sendToUsers(messenger, message, users));
        }
    }

    private void sendToUsers(Messenger messenger, ZMessage message, Collection<ZUser> users) {
        users.forEach(user -> {
            ZUserConfig userConfig = user.getUserConfig();
            if (userConfig != null) {
                messenger.sendToUser(message.getMessage(res, userConfig.getLocale()), user);
            }
        });
    }

}
