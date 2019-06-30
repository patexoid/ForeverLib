package com.patex.messaging;

import static java.util.function.Predicate.not;

import com.patex.entities.ZUserConfig;
import com.patex.entities.ZUserConfigRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.concurrent.DelegatingSecurityContextCallable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@RequiredArgsConstructor
public class MessengerToTelegramMesengerListenerHandler implements TelegramMessengerListener {

  private final Collection<MessengerListener> listeners;
  private final ZUserConfigRepository userConfigRepository;

  @Override
  @SneakyThrows
  public Stream<SendMessage> createResponse(Message request) {
    Long chatId = request.getChatId();
    String requestMessage = request.getText().trim();

    Optional<SecurityContext> securityContext = authenticate(chatId);
    if (securityContext.isPresent()) {
      List<String> responseMessages = new DelegatingSecurityContextCallable<>(
          () -> listeners.stream()
              .flatMap(l -> l.createResponse(requestMessage))
              .collect(Collectors.toList()),
          securityContext.get()).call();
      return responseMessages.stream().map(r -> createSendMessage(chatId, r));
    } else {
      return listeners.stream()
          .filter(not(MessengerListener::requireUserAuth))
          .flatMap(l -> l.createResponse(requestMessage))
          .map(r -> createSendMessage(chatId, r));
    }
  }

  private Optional<SecurityContext> authenticate(long chatId) {
    Optional<ZUserConfig> userConfig = userConfigRepository.findByTelegramChatId(chatId);
    return userConfig
        .map(ZUserConfig::getUser)
        .map(u -> new UsernamePasswordAuthenticationToken(u.getUsername(), u.getPassword(), u.getAuthorities()))
        .map(SecurityContextImpl::new);
  }

  private SendMessage createSendMessage(Long chatId, String r) {
    SendMessage response = new SendMessage(chatId, r);
    response.setParseMode("HTML");
    return response;
  }
}
