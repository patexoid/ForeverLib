package com.patex.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@RunWith(MockitoJUnitRunner.class)
public class TelegramBotTest {


    private TelegramBot telegramBot;

    @Mock
    private TelegramMessengerListener listener;

    @Before
    public void setUp() {
        telegramBot = spy(new TelegramBot("42", "42", Collections.singleton(listener)));
        doReturn(null).when(telegramBot).executeNoEx(any(SendMessage.class));
    }

    @Test
    public void shouldNoResponce() {
        Update update = mock(Update.class);
        telegramBot.onUpdateReceived(update);
        verify(telegramBot, never()).executeNoEx(any(SendMessage.class));
    }

    @Test
    public void shouldResponce() {
        Update update = mock(Update.class);
        when(update.hasMessage()).thenReturn(true);
        Message updateMessage = mock(Message.class);
        when(update.getMessage()).thenReturn(updateMessage);
        SendMessage message = mock(SendMessage.class);
        when(listener.createResponse(updateMessage)).thenReturn(Stream.of(message));
        telegramBot.onUpdateReceived(update);
        verify(telegramBot).executeNoEx(message);
    }
}
