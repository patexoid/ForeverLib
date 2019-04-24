package com.patex.lrequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.patex.entities.Author;
import com.patex.lrequest.actionprocessor.FindAuthor;
import com.patex.lrequest.actionprocessor.GetField;
import com.patex.lrequest.actionprocessor.GetFirst;
import com.patex.service.AuthorService;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ParserTest {

  public static final String DESCRIPTION = "description";
  public static final String AUTHOR_NAME = "authorName";
  private ActionBuilder actionBuilder;
  private AuthorService authorService;

  @Before
  public void setUp() {
    authorService = mock(AuthorService.class);
    Author author = new Author();
    author.setDescr(DESCRIPTION);
    when(authorService.findByName(AUTHOR_NAME)).thenReturn(Collections.singletonList(author));
    FindAuthor findAuthor = new FindAuthor(authorService);
    Map<String,ActionHandler> handlerMap=new HashMap<>();
    handlerMap.put("findAuthor",findAuthor);
    handlerMap.put("getField",new GetField());
    handlerMap.put("getFirst",new GetFirst());
    ActionHandlerStorage handlerStorage = new ActionHandlerStorage(handlerMap);
    actionBuilder = new ActionBuilder(handlerStorage);
  }


  @Test
  public void testParser() throws Exception {
    String request="findAuthor(\""+AUTHOR_NAME+"\").getField(\"descr\").getFirst";
    Value result = actionBuilder.execute(request);
    assertEquals(DESCRIPTION,result.getResultSupplier().get());

  }
}
