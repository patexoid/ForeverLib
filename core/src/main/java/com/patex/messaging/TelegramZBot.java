package com.patex.messaging;

import com.patex.entities.ZUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 */

@Component
public class TelegramZBot extends TelegramLongPollingBot implements Messenger {

    public static final int MAX_MESSAGE_SIZE = 4000;

    private static Logger log = LoggerFactory.getLogger(TelegramZBot.class);

    static {
        ApiContextInitializer.init(); //strange magic
    }

    private final String botName;
    private final String botToken;
    private String baseurl;
    @Autowired
    private MessengerService messagingComponent;
    private List<String> delims = Arrays.asList("\n", ".", ";", "-", ",");


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
            List<String> messages = splitMessage(message);
            for (String s : messages) {
                sendMessageToChat(s, user.getUserConfig().getTelegramChatId());
            }
        }
    }

    private List<String> splitMessage(String message) {
        List<String> messages = createMessageStream(message, "\n").collect(Collectors.toList());
        List<String> result = new ArrayList<>(Collections.singleton(""));
        for (String s : messages) {
            int lastIndex = result.size() - 1;
            String lastElem = result.get(lastIndex);
            if (lastElem.length() + s.length() < MAX_MESSAGE_SIZE) {
                result.set(lastIndex, lastElem + s);
            } else {
                result.add(s);
            }
        }
        return result;
    }


    private Stream<String> createMessageStream(String message, String delim) {
        if (message.length() < MAX_MESSAGE_SIZE) {
            return Stream.of(message);
        } else {
            StringTokenizer st = new StringTokenizer(message, delim,true);
            if (st.countTokens() > 1) {
                return getStream(st).flatMap(s -> createMessageStream(s, delim));
            } else {
                int index = delims.indexOf(delim);
                if (index < delims.size()) {
                    return createMessageStream(message, delims.get(index) + 1);
                } else {
                    Stream<String> first =
                            createMessageStream(message.substring(0, message.length() / 2), delim);
                    Stream<String> second =
                            createMessageStream(message.substring(message.length() / 2), delim);
                    return Stream.concat(first, second);
                }
            }
        }
    }

    private Stream<String> getStream(StringTokenizer st) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(new Iterator<String>() {
                    @Override
                    public boolean hasNext() {
                        return st.hasMoreTokens();
                    }

                    @Override
                    public String next() {
                        return st.nextToken();
                    }
                }, Spliterator.ORDERED),
                false);
    }

    private void sendMessageToChat(String message, Long chatId) {
        if(chatId==null){
            return;
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(message);
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("HTML");
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            log.warn(e.getMessage(),e);
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
