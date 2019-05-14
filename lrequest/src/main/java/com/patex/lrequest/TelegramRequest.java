package com.patex.lrequest;

import com.patex.messaging.MessengerListener;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TelegramRequest implements MessengerListener {

  private final ActionBuilder actionBuilder;

  @Override
  @Transactional(readOnly = true)
  public Stream<String> createResponse(String request) {
    try {

      Value result = actionBuilder.execute(request);
      Object value = result.getResultSupplier().get();
      if(value instanceof Stream) {
        return ((Stream<?>) value).map(String::valueOf).collect(Collectors.toList()).stream();
      } else{
        return Stream.of(String.valueOf(value));
      }
    } catch (ParseException e) {
      return Stream.empty();
    }
  }

  @Override
  public boolean requireUserAuth() {
    return true;
  }
}
