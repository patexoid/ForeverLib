package com.patex.lrequest.actionprocessor;

import com.patex.lrequest.ActionHandler;
import com.patex.lrequest.ActionResult;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class Val implements ActionHandler<String> {

  @Override
  public ActionResult execute(Supplier... params) {
    return new ActionResult<>(o -> (String)params[0].get(), String.class);
  }

  @Override
  public boolean isApplicableParams(Class[] types) {
    return types.length==1&& String.class.isAssignableFrom(types[0]);
  }

  @Override
  public boolean isApplicableData(Class type) {
    return false;
  }
}
