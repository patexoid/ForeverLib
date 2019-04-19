package com.patex.lrequest.actionprocessor;

import static com.patex.lrequest.ResultType.Type.One;

import com.patex.lrequest.LazyActionHandler;
import com.patex.lrequest.RequestResult;
import com.patex.lrequest.ResultType;
import com.patex.lrequest.WrongActionSyntaxException;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class Val implements LazyActionHandler<String> {

  @Override
  public Function execute(Supplier... params) {
    return o -> (String) params[0].get();
  }

  @Override
  public ResultType preprocess(ResultType input, RequestResult... paramTypes) {
    if (!(paramTypes.length != 1 && Void.class.isAssignableFrom(input.getReturnType()))) {
      throw new WrongActionSyntaxException("Void.Val(\"String Value\")");
    }
    return new ResultType(One, String.class);
  }
}
