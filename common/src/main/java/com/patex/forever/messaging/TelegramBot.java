package com.patex.forever.messaging;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 *
 */

public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);

    private final String botName;
    private final String botToken;
    private BiFunction<String, Long, Optional<String>> responseF;

    TelegramBot(String botToken, String botName, BiFunction<String, Long, Optional<String>> response) {
        this.botToken = botToken;
        this.botName = botName;
        this.responseF = response;
    }

    @SneakyThrows
    public void start() {
        if (botToken != null && !botToken.isEmpty()) {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
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
            Long chatId = message.getChatId();
            responseF.apply(message.getText().trim(), chatId)
                    .ifPresent(responseMessage -> sendMessageToChat(responseMessage, chatId));
        }
    }

    public void sendMessageToChat(String message, Long chatId) {
        try {
            SendMessage sendMessage = new SendMessage(""+chatId, message);
            sendMessage.setParseMode("HTML");
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.warn(e.getMessage(), e);
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }
}
