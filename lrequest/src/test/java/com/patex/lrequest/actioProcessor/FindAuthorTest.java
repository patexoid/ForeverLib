package com.patex.lrequest.actioProcessor;

import com.patex.lrequest.ResultType;
import com.patex.lrequest.ResultType.Type;
import com.patex.lrequest.actionprocessor.FindAuthor;
import org.junit.Test;

public class FindAuthorTest {

  @Test
  public void testValidateWithInputString() {
    FindAuthor findAuthor = new FindAuthor(null);
    findAuthor.preprocess(new ResultType(Type.None, String.class));
  }
}
