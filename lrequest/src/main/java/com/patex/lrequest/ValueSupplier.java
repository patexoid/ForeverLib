package com.patex.lrequest;

import java.util.function.Supplier;

public interface ValueSupplier {

  public Supplier getValueSupplier(ActionHandlerStorage handlerStorage);
}
