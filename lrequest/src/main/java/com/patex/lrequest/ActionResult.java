package com.patex.lrequest;

import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ActionResult<I, O> {

  private final Function<I, O> resultFunc;

  private final FlowType flowType;
}
