package com.patex.lrequest;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GetFirst implements ActionProcessor {

  @Override
  public ActionResult execute(Object... params) {
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
