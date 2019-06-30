package com.patex.lrequest;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActionHandlerStorage {

  private final Map<String, ActionHandler> actions;

  public ActionHandler getAction(String key) {
    return actions.get(key);
  }
}
