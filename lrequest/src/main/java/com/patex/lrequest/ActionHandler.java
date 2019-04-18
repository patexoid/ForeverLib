package com.patex.lrequest;

import java.util.function.Supplier;

public interface ActionHandler<R> {

  ActionResult execute(Supplier... params);

  boolean isApplicableParams(Class... types);

  boolean isApplicableData(Class type);
}
