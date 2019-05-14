package com.patex.lrequest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ActionBuilder {

  private final ActionHandlerStorage actionHandlerStorage;

  public Value execute(String requestString) throws ParseException {
    java.io.InputStream is = new java.io.ByteArrayInputStream(requestString.getBytes());
    LibRequestBuilder t = new LibRequestBuilder(is, java.nio.charset.Charset.forName("UTF-8"));
    Request request = t.Request();

    return request.getValueSupplier(actionHandlerStorage);
  }

}
