package com.patex.messaging;

import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
public class TelegramCreateChatWithUser implements TelegramMessengerListener {

    private final String baseUrl;

    public TelegramCreateChatWithUser(@Value("${telegram.bot.baseurl:}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public Stream<SendMessage> createResponse(Message request) {
        if (request.getText().trim().equalsIgnoreCase("/subscribe")) {
            Long chatId = request.getChatId();
            String url = baseUrl + "/user/updateConfig?telegramChatId=" + chatId;

            SendMessage response = new SendMessage(chatId, "Please open " + url + " to register chat");
            response.setParseMode("HTML");
            return Stream.of(response);
        } else {
            return Stream.empty();
        }
    }
}
