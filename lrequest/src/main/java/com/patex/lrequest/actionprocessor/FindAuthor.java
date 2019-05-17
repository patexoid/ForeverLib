package com.patex.lrequest.actionprocessor;

import com.patex.entities.Author;
import com.patex.lrequest.ActionHandler;
import com.patex.lrequest.ActionResult;
import com.patex.lrequest.DataType;
import com.patex.lrequest.DataType.Type;
import com.patex.lrequest.Value;
import com.patex.lrequest.WrongActionSyntaxException;
import com.patex.service.AuthorService;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class FindAuthor implements ActionHandler {

  private final AuthorService service;

  private final List<AuthorAction> authorActions = Arrays.asList(new InitialAuthorAction(), new StreamAuthorAction());

  public FindAuthor(AuthorService service) {
    this.service = service;
  }

  @Override
  public ActionResult createFuncton(DataType dataType, Value... values)
      throws WrongActionSyntaxException {

    return authorActions.stream()
        .filter(aa -> aa.match(dataType, values))
        .findFirst()
        .map(aa -> aa.createFunction(dataType, values))
        .orElseThrow(() -> new WrongActionSyntaxException(
            "String.FindAuthor() or FindAuthor(String)"));
  }

  private interface AuthorAction {

    boolean match(DataType dataType, Value[] values);

    ActionResult createFunction(DataType dataType, Value... values);
  }

  private class StreamAuthorAction implements AuthorAction {

    @Override
    public boolean match(DataType dataType, Value[] values) {
      return dataType.is(Type.stream)
          && String.class.isAssignableFrom(dataType.getReturnType())
          && values.length == 0;
    }

    @Override
    public ActionResult createFunction(DataType dataType, Value... values) {
      return new ActionResult<String, Stream<Author>>(
          s -> s.map(service::findByName)
              .flatMap(Collection::stream),
          DataType.streamResult(Author.class));
    }
  }

  private class InitialAuthorAction implements AuthorAction {

    @Override
    public boolean match(DataType dataType, Value[] values) {
      return dataType.is(Type.initial)
          && values.length == 1
          && String.class.isAssignableFrom(values[0].getResultClass());
    }

    @Override
    public ActionResult createFunction(DataType dataType, Value... values) {
      return new ActionResult<>(o -> {
        Value value = values[0];
        String authorPrefix = (String) value.getResultSupplier().get();
        return service.findByName(authorPrefix).stream();
      }, DataType.streamResult(Author.class));
    }
  }
}
