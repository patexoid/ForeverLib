package com.patex.lrequest.actionprocessor;

import com.patex.entities.Author;
import com.patex.lrequest.ActionHandler;
import com.patex.lrequest.ActionResult;
import com.patex.service.AuthorService;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class FindAuthor implements ActionHandler {

  private final AuthorService service;

  public FindAuthor(AuthorService service) {
    this.service = service;
  }

  @Override
  public ActionResult<String, List> execute(Supplier... params) {

    Function<String, List> function = input -> {
      String authorName;
      if (params.length == 1) {
        authorName = (String)params[0].get();
      } else {
        authorName = input;
      }
      return service.findByName(authorName);
    };
    return new ActionResult<>(function, List.class, Author.class);
  }


  @Override
  public boolean isApplicableParams(Class[] types) {
    return types.length == 0 || (types.length == 1 && String.class.isAssignableFrom(types[0]));
  }

  @Override
  public boolean isApplicableData(Class type) {
    return String.class.equals(type) || Void.class.equals(type);
  }
}
