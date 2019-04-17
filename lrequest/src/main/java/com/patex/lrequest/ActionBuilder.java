package com.patex.lrequest;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class ActionBuilder {

  private final Map<String, ActionProcessor> actions;

  public ActionBuilder(List<ActionProcessor> actions) {
    this.actions = actions.stream().collect(Collectors.toMap(a -> a.getClass().getSimpleName(), o -> o));
  }

  public Object exetute(String requestString) throws ParseException {
    java.io.InputStream is = new java.io.ByteArrayInputStream(requestString.getBytes());
    LibRequestBuilder t = new LibRequestBuilder(is, java.nio.charset.Charset.forName("UTF-8"));
    SimpleNode request = t.Request();

    Function result = o -> o;
    for (Node n : request.children) {
      Action action = (Action) n;
      ActionProcessor actionProcessor = actions.get(action.jjtGetValue());
      Object[] values;
      if (action.children != null) {
        values = Stream.of(action.children).map(o -> ((Param) o).getValue()).toArray();
      } else {
        values = new Object[0];
      }
      ActionResult actionResult = actionProcessor.execute(values);
      result = result.andThen(actionResult.getResult());
    }
    return result.apply(null);
  }

}
