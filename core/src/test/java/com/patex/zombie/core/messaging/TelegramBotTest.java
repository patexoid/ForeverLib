package com.patex.zombie.core.messaging;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TelegramBotTest {


    private static final String MESSAGE = "message";
    private static final long CHAT_ID = 458;
    public static final String RESPONSE_MESSAGE = "response message";
    private TelegramBot telegramBot;

    @Mock
    private BiFunction<String, Long, Optional<String>> responseF;


    @Before
    public void setUp() {
        telegramBot = spy(new TelegramBot("42", "42", responseF));
    }

    @Test
    public void shouldNoAction() {
        Update update = mock(Update.class);
        when(update.hasMessage()).thenReturn(false);
        telegramBot.onUpdateReceived(update);
        verifyZeroInteractions(responseF);
    }

    @Test
    public void shouldNoResponce() {
        Update update = mock(Update.class);
        when(update.hasMessage()).thenReturn(true);
        Message message = mock(Message.class);
        when(update.getMessage()).thenReturn(message);
        when(message.getText()).thenReturn(MESSAGE);
        when(message.getChatId()).thenReturn(CHAT_ID);
        when(responseF.apply(MESSAGE, CHAT_ID)).thenReturn(Optional.empty());
        telegramBot.onUpdateReceived(update);
        verify(telegramBot, never()).sendMessageToChat(any(), eq(CHAT_ID));
    }


    @Test
    public void shouldResponce() {
        Update update = mock(Update.class);
        when(update.hasMessage()).thenReturn(true);
        Message message = mock(Message.class);
        when(update.getMessage()).thenReturn(message);
        when(message.getText()).thenReturn(MESSAGE);
        when(message.getChatId()).thenReturn(CHAT_ID);
        when(responseF.apply(MESSAGE, CHAT_ID)).thenReturn(Optional.of(RESPONSE_MESSAGE));
        telegramBot.onUpdateReceived(update);
        verify(telegramBot).sendMessageToChat(RESPONSE_MESSAGE, CHAT_ID);
    }

}
