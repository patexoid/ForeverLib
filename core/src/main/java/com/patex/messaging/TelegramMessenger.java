package com.patex.messaging;

import com.google.common.annotations.VisibleForTesting;
import com.patex.entities.ZUser;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/**
 *
 */

@Component
@ConditionalOnExpression("!'${telegram.bot.token}'.isEmpty()")
public class TelegramMessenger implements Messenger {

    private static final int MAX_MESSAGE_SIZE = 4000;
    private static final List<String> DELIMS = Arrays.asList("\n", ".", ";", "-", ",");
    private final TelegramBot telegramBot;
    private final TextSpliterator spliterator;

    @Autowired
    public TelegramMessenger(@Value("${telegram.bot.token}") String botToken,
        @Value("${telegram.bot.name}") String botName, Collection<TelegramMessengerListener> listeners) {
        telegramBot = new TelegramBot(botName, botToken, listeners);
        spliterator = new TextSpliterator(MAX_MESSAGE_SIZE, DELIMS);
    }

    @VisibleForTesting
    TelegramMessenger(TelegramBot telegramBot, TextSpliterator spliterator) {
        this.telegramBot = telegramBot;
        this.spliterator = spliterator;
    }

    @PostConstruct
    public void start() {
        telegramBot.start();
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
