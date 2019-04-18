package com.patex.lrequest;

import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ActionResult<I, O> {

  private final Function<I, O> result;

  private final Class<O> type;
  private final Class collectionElementType;

  public ActionResult(Function<I, O> result, Class<O> type) {
    this.result = result;
    this.type = type;
    collectionElementType=null;
  }
}
