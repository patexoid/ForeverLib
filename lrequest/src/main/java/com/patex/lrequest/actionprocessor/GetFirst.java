package com.patex.lrequest.actionprocessor;

import com.patex.lrequest.ActionHandler;
import com.patex.lrequest.ActionResult;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class GetFirst implements ActionHandler {

  @Override
  public ActionResult execute(Supplier... params) {
    return new ActionResult(l -> ((List) l).get(0), Object.class);
  }

  @Override
  public boolean isApplicableParams(Class[] types) {
    return types.length == 0;
  }

  @Override
  public boolean isApplicableData(Class type) {
    return List.class.isAssignableFrom(type);
  }
}
