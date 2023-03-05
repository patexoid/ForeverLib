package com.patex.messaging;

import com.patex.zombie.model.User;
import com.patex.zombie.model.UserConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TelegramMessengerTest {

    public static final long TELEGRAM_CHAT_ID = 424224242L;
    public static final String MESSAGE = "message";
    public static final String BASE_URL = "baseUrl";
    @Mock
    private MessengerService messagingComponent;

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private TextSpliterator spliterator;


    private TelegramMessenger telegramMessenger;

    @BeforeEach
    public void setUp() {
        telegramMessenger = new TelegramMessenger(messagingComponent, telegramBot, BASE_URL, spliterator);
    }

    @Test
    public void shouldStart() {
        telegramMessenger.start();

        verify(messagingComponent).register(telegramMessenger);
        verify(telegramBot).start();
    }

    @Test
    public void shouldSendMessage() {
        User user = new User();
        UserConfig config = new UserConfig();
        config.setTelegramChatId(TELEGRAM_CHAT_ID);
        user.setUserConfig(config);
        when(spliterator.splitText(MESSAGE)).thenReturn(Collections.singletonList(MESSAGE));

        telegramMessenger.sendToUser(MESSAGE, user);

        verify(telegramBot).sendMessageToChat(MESSAGE, TELEGRAM_CHAT_ID);
    }

    @Test
    public void shouldSendMessages() {
        String part1 = "part1";
        String part2 = "part2";
        User user = new User();
        UserConfig config = new UserConfig();
        config.setTelegramChatId(TELEGRAM_CHAT_ID);
        user.setUserConfig(config);
        when(spliterator.splitText(MESSAGE)).thenReturn(Arrays.asList(part1, part2));

        telegramMessenger.sendToUser(MESSAGE, user);

        verify(telegramBot).sendMessageToChat(part1, TELEGRAM_CHAT_ID);
        verify(telegramBot).sendMessageToChat(part2, TELEGRAM_CHAT_ID);
    }

    @Test
    public void shouldSendResponse() {
        Optional<String> response = telegramMessenger.response("/subscribe", TELEGRAM_CHAT_ID);
        assertTrue(response.isPresent());
        String expectedMessage = "Please open <a href=\"baseUrl/user/updateConfig?telegramChatId=424224242\">Link</a>" +
                " to register chat";
        assertEquals(expectedMessage, response.get());
    }

    @Test
    public void shouldNotSendResponse() {
        Optional<String> response = telegramMessenger.response("blah blah", TELEGRAM_CHAT_ID);
        assertFalse(response.isPresent());
    }
}
