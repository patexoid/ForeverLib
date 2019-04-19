package com.patex.lrequest;

import java.util.function.Function;
import java.util.function.Supplier;

public interface LazyActionHandler<R> extends ActionHandler{

  ResultType preprocess(ResultType input, RequestResult... paramTypes);

  Function execute(Supplier... params);

  @Override
  default ActionResult execute(Supplier[] params, ResultType input, RequestResult... paramTypes) {
    return new ActionResult(execute(params),preprocess(input, paramTypes));
  }
}
