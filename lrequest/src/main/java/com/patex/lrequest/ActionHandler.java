package com.patex.lrequest;

import java.util.function.Function;
import java.util.function.Supplier;

public interface ActionHandler<R> {

  ActionResult execute(Supplier[] params, ResultType input, RequestResult... paramTypes);

}
