package com.patex.lrequest;

import java.util.function.Supplier;

public interface ValueSupplier {

  RequestResult getValueSupplier(ActionHandlerStorage handlerStorage);
}
