package com.patex.messaging;

import com.google.common.annotations.VisibleForTesting;
import com.patex.entities.ZUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 *
 */

@Component
@ConditionalOnExpression("!'${telegram.bot.token}'.isEmpty()")
public class TelegramMessenger implements Messenger {

    private static final int MAX_MESSAGE_SIZE = 4000;
    private static final List<String> DELIMS = Arrays.asList("\n", ".", ";", "-", ",");
    private final MessengerService messagingComponent;
    private final TelegramBot telegramBot;
    private final String baseUrl;
    private final TextSpliterator spliterator;

    @Autowired
    public TelegramMessenger(@Value("${telegram.bot.token}") String botToken,
                             @Value("${telegram.bot.name}") String botName,
                             @Value("${telegram.bot.baseurl}") String baseurl,
                             MessengerService messagingComponent) {
        this.baseUrl = baseurl;
        this.messagingComponent = messagingComponent;
        telegramBot = new TelegramBot(botToken, botName, this::response);
        spliterator = new TextSpliterator(MAX_MESSAGE_SIZE, DELIMS);
    }

    @VisibleForTesting
    TelegramMessenger(MessengerService messagingComponent, TelegramBot telegramBot,
                      String baseUrl, TextSpliterator spliterator) {
        this.messagingComponent = messagingComponent;
        this.telegramBot = telegramBot;
        this.baseUrl = baseUrl;
        this.spliterator = spliterator;
    }

    @PostConstruct
    public void start() {
        telegramBot.start();
        messagingComponent.register(this);
    }

    Optional<String> response(String request, Long chatId) {
        if ("/subscribe".equalsIgnoreCase(request)) {
            String url = baseUrl + "/user/updateConfig?telegramChatId=" + chatId;
            return Optional.of("Please open <a href=\"" + url + "\">Link</a> to register chat");
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void sendToUser(String message, ZUser user) {
        if (user.getUserConfig() != null) {
            sendToUser(message, user.getUserConfig().getTelegramChatId());
        }
    }

    private void sendToUser(String message, Long telegramChatId) {
        List<String> messages = spliterator.splitText(message);
        messages.forEach(s -> telegramBot.sendMessageToChat(s, telegramChatId));
    }
}
