package com.patex.lrequest;

public interface ActionHandler {

  ActionResult createFuncton(DataType input, Value... values)
      throws WrongActionSyntaxException;

}
