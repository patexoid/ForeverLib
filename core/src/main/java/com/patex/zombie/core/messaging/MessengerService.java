package com.patex.zombie.core.messaging;

import com.patex.model.User;
import com.patex.zombie.core.service.Resources;
import com.patex.zombie.core.utils.Res;
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

    private final Resources res;

    private final List<Messenger> messengers = new ArrayList<>();

    public MessengerService(Resources res) {
        this.res = res;
    }

    public void register(Messenger messenger) {
        messengers.add(messenger);
        toRole(new Res("lib.started"), null, Collections.singletonList(messenger));
    }

    @PreDestroy()
    public void stopEvent() {
        toRole(new Res("lib.stopped"), null);
    }

    public void sendMessageToUser(Res message, User user) {
        System.out.println(res);
        System.out.println(message);
        System.out.println(user);
//        if (user != null && user.getUserConfig() != null) {
//            Locale locale = user.getUserConfig().getLocale();
//            String messageS = message.getMessage(res, locale);
//            messengers.forEach(messenger -> messenger.sendToUser(messageS, user));
//        }
    }

    public void toRole(Res message, String role) {
        toRole(message, role, messengers);
    }

    private void toRole(Res message, String role, Collection<Messenger> messengers) {

        System.out.println(message + role + messengers);
//        Collection<UserEntity> users = userService.getByRole(role);
//        messengers.forEach(messenger -> users.forEach(user -> sendMessageToUser(message, user)));
    }
}
