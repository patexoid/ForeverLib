package com.patex.messaging;

import java.io.Serializable;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 *
 */
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);

    static {
        ApiContextInitializer.init(); //strange magic
    }

    private final String botName;
    private final String botToken;
    private final Collection<TelegramMessengerListener> messengerListeners;

    public void start() {
        if (botToken != null && !botToken.isEmpty()) {
            TelegramBotsApi botsApi = new TelegramBotsApi();
            try {
                botsApi.registerBot(this);
            } catch (TelegramApiException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();

            messengerListeners.stream().flatMap(l -> l.createResponse(message))
                .forEach(this::executeNoEx);
        }
    }


    <T extends Serializable, Method extends BotApiMethod<T>> T executeNoEx(Method method) {
        try {
            return super.execute(method);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    void sendMessageToChat(String message, Long chatId) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.setParseMode("HTML");
        executeNoEx(sendMessage);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}
