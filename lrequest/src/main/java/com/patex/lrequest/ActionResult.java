package com.patex.lrequest;

import java.util.function.Function;

public class ActionResult<I, O> {

  private final Function<I, O> result;

  private final Class<O> type;

  public ActionResult(Function<I, O> result, Class<O> type) {
    this.result = result;
    this.type = type;
  }

  public Function<I, O> getResult() {
    return result;
  }

  public Class<O> getType() {
    return type;
  }
}
