package com.patex.messaging;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.patex.entities.ZUser;
import com.patex.entities.ZUserConfig;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TelegramMessengerTest {

    public static final long TELEGRAM_CHAT_ID = 424224242L;
    public static final String MESSAGE = "message";
    public static final String BASE_URL = "baseUrl";
    @Mock
    private TelegramBot telegramBot;

    @Mock
    private TextSpliterator spliterator;


    private TelegramMessenger telegramMessenger;

    @Before
    public void setUp() {
        telegramMessenger = new TelegramMessenger(telegramBot, spliterator);
    }

    @Test
    public void shouldStart() {
        telegramMessenger.start();

        verify(telegramBot).start();
    }

    @Test
    public void shouldSendMessage() {
        ZUser user = new ZUser();
        ZUserConfig config = new ZUserConfig();
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
        ZUser user = new ZUser();
        ZUserConfig config = new ZUserConfig();
        config.setTelegramChatId(TELEGRAM_CHAT_ID);
        user.setUserConfig(config);
        when(spliterator.splitText(MESSAGE)).thenReturn(Arrays.asList(part1, part2));

        telegramMessenger.sendToUser(MESSAGE, user);

        verify(telegramBot).sendMessageToChat(part1, TELEGRAM_CHAT_ID);
        verify(telegramBot).sendMessageToChat(part2, TELEGRAM_CHAT_ID);
    }

//    @Test
//    public void shouldSendResponse() {
//        Optional<String> response = telegramMessenger.response("/subscribe", TELEGRAM_CHAT_ID);
//        Assert.assertTrue(response.isPresent());
//        String expectedMessage = "Please open <a href=\"baseUrl/user/updateConfig?telegramChatId=424224242\">Link</a>" +
//                " to register chat";
//        Assert.assertEquals(expectedMessage, response.get());
//    }
//
//    @Test
//    public void shouldNotSendResponse() {
//        Optional<String> response = telegramMessenger.response("blah blah", TELEGRAM_CHAT_ID);
//        Assert.assertFalse(response.isPresent());
//    }
}
