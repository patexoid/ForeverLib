package com.patex.lrequest;

import java.util.function.Supplier;

public interface ActionProcessor<R> {

  ActionResult execute(Object... params);

  boolean isApplicableParams(Class... types);

  boolean isApplicableData(Class type);
}
