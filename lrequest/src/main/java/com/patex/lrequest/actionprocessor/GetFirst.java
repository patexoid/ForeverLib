package com.patex.lrequest.actionprocessor;

import com.patex.lrequest.ActionHandler;
import com.patex.lrequest.ActionResult;
import com.patex.lrequest.FlowType;
import com.patex.lrequest.FlowType.Type;
import com.patex.lrequest.Value;
import com.patex.lrequest.WrongActionSyntaxException;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class GetFirst implements ActionHandler {

  @Override
  public ActionResult createFuncton(FlowType input, Value... values)
      throws WrongActionSyntaxException {
    if (!input.is(Type.stream) || values.length != 0) {
      throw new WrongActionSyntaxException("Stream.GetFirst");
    }

    return new ActionResult<>(l -> ((Stream<?>) l).findFirst().orElse(null),
        FlowType.objResult(input.getReturnType()));
  }

}
