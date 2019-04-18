package com.patex.lrequest;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ActionBuilder {

  private final ActionHandlerStorage actionHandlerStorage;

  public Object exetute(String requestString) throws ParseException {
    java.io.InputStream is = new java.io.ByteArrayInputStream(requestString.getBytes());
    LibRequestBuilder t = new LibRequestBuilder(is, java.nio.charset.Charset.forName("UTF-8"));
    SimpleNode request = t.Request();

    return ((Request) request).getValueSupplier(actionHandlerStorage).get();
  }

}
