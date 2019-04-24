package com.patex.lrequest.actionprocessor;

import com.patex.entities.Author;
import com.patex.lrequest.ActionHandler;
import com.patex.lrequest.ActionResult;
import com.patex.lrequest.FlowType;
import com.patex.lrequest.FlowType.Type;
import com.patex.lrequest.Value;
import com.patex.lrequest.WrongActionSyntaxException;
import com.patex.service.AuthorService;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class FindAuthor implements ActionHandler {

  private final AuthorService service;

  public FindAuthor(AuthorService service) {
    this.service = service;
  }


  @Override
  public ActionResult createFuncton(FlowType flowType, Value... values)
      throws WrongActionSyntaxException {

    if ((flowType.is(Type.stream) || flowType.is(Type.object)) &&
        (!String.class.isAssignableFrom(flowType.getReturnType()) || values.length != 0)
        ||
        flowType.is(Type.initial) && (values.length != 1 || !String.class.isAssignableFrom(values[0].getResultClass()))
    ) {
      throw new WrongActionSyntaxException("String.FindAuthor() or Stream<String>.FindAuthor or FindAuthor(String)");
    }

    FlowType newFlowType = FlowType.streamResult(Author.class);
    if (flowType.is(Type.stream)) {
      return new ActionResult<Stream<String>, Stream<Author>>(
          s -> s.map(service::findByName).flatMap(Collection::stream), newFlowType);
    } else if (flowType.is(Type.object)) {
      Function<String, List<Author>> findByName = service::findByName;
      return new ActionResult<>(findByName.andThen(Collection::stream),
          newFlowType);
    } else if (flowType.is(Type.initial)) {
      return new ActionResult<>(o -> {
        Value value = values[0];
        String authorPrefix = (String) value.getResultSupplier().get();
        return service.findByName(authorPrefix).stream();
      }, newFlowType);
    }
    throw new WrongActionSyntaxException("String.FindAuthor() or Stream<String>.FindAuthor or FindAuthor(String)");
  }


}
