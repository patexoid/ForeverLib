package com.patex.messaging;

import java.util.stream.Stream;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramMessengerListener {

    Stream<SendMessage> createResponse(Message request);

}
