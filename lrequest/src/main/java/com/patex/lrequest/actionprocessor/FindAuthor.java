package com.patex.lrequest.actionprocessor;

import static com.patex.lrequest.ResultType.Type.FlatMap;

import com.patex.entities.Author;
import com.patex.lrequest.LazyActionHandler;
import com.patex.lrequest.RequestResult;
import com.patex.lrequest.ResultType;
import com.patex.lrequest.WrongActionSyntaxException;
import com.patex.service.AuthorService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class FindAuthor implements LazyActionHandler {

  private final AuthorService service;

  public FindAuthor(AuthorService service) {
    this.service = service;
  }

  @Override
  public Function<Object, Stream> execute(Supplier... params) {
    if (true) {
      Author a = new Author();
      a.setName("dsdsdsdsd");
      return o -> Stream.of(a);
    }
    return input -> {
      String authorName;
      if (params.length == 1) {
        authorName = (String) params[0].get();
      } else {
        authorName = (String) input;
      }
      return service.findByName(authorName).stream();
    };
  }

  @Override
  public ResultType preprocess(ResultType input, RequestResult... paramTypes) {
    if (
        (!(Void.class.isAssignableFrom(input.getReturnType()) &&
            paramTypes.length == 1 &&
            String.class.isAssignableFrom(paramTypes[0].getResultClass()))
            &&
            !(String.class.isAssignableFrom(input.getReturnType()) &&
                paramTypes.length == 0))
    ) {
      throw new WrongActionSyntaxException("String.FindAuthor() or Void.Find(String)");
    }
    return new ResultType(FlatMap, Author.class);
  }
}
