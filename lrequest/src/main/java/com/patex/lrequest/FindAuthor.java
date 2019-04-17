package com.patex.lrequest;

import com.patex.entities.Author;
import com.patex.service.AuthorService;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;

@Service
public class FindAuthor implements ActionProcessor {

  private final AuthorService service;

  public FindAuthor(AuthorService service) {
    this.service = service;
  }

  @Override
  public ActionResult<Void, List<Author>> execute(Object... params) {

    Function<Void, List<Author>> function = nothing -> service.findByName((String) params[0]);
    return new ActionResult(function, List.class );
  }

  @Override
  public boolean isApplicableParams(Class[] types) {
    return types.length == 1&& String.class.isAssignableFrom(types[0]);
  }

  @Override
  public boolean isApplicableData(Class type) {
    return Void.class.equals(type);
  }
}
