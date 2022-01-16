package com.patex.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TelegramBotTest {


    public static final String RESPONSE_MESSAGE = "response message";
    private static final String MESSAGE = "message";
    private static final long CHAT_ID = 458;
    private TelegramBot telegramBot;

    @Mock
    private BiFunction<String, Long, Optional<String>> responseF;


    @BeforeEach
    public void setUp() {
        telegramBot = spy(new TelegramBot("42", "42", responseF));
    }

    @Test
    public void shouldNoAction() {
        Update update = mock(Update.class);
        when(update.hasMessage()).thenReturn(false);
        telegramBot.onUpdateReceived(update);
        verifyNoInteractions(responseF);
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
