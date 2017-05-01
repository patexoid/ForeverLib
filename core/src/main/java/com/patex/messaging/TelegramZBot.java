package com.patex.messaging;

import com.patex.entities.ZUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;

/**
 *
 */

@Component
public class TelegramZBot extends TelegramLongPollingBot implements Messenger {

    static {
        ApiContextInitializer.init(); //strange magic
    }

    private final String botName;

    private String baseurl;

    @Autowired
    private MessengerService messagingComponent;

    private final String botToken;


    public TelegramZBot(@Value("${telegram.bot.token}") String botToken,
                        @Value("${telegram.bot.name}") String botName,
                        @Value("${telegram.bot.baseurl}") String baseurl) {
        this.botToken = botToken;
        this.botName = botName;
        this.baseurl = baseurl;
    }

    @PostConstruct
    public void start() {
        if (botToken != null && !botToken.isEmpty()) {
            TelegramBotsApi botsApi = new TelegramBotsApi();
            try {
                botsApi.registerBot(this);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            messagingComponent.register(this);
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
            if ("/subscribe".equalsIgnoreCase(message.getText().trim())) {
                Long chatId = message.getChatId();
                String url = baseurl + "/user/updateConfig?telegramChatId=" + chatId;
                sendMessageToChat("Please open <a href=\"" + url + "\">Link</a> to register chat", chatId);
            }
        }
    }

    @Override
    public void sendToUser(String message, ZUser user) {
        if (user.getUserConfig() != null) {
            sendMessageToChat(message, user.getUserConfig().getTelegramChatId());
        }
    }

    private void sendMessageToChat(String message, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(message);
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("HTML");
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onClosing() {

    }
}
