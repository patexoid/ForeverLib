package com.patex.lrequest;

public interface ActionHandler {

  ActionResult createFuncton(FlowType input, Value... values)
      throws WrongActionSyntaxException;

}
