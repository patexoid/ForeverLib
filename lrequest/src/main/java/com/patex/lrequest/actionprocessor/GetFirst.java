package com.patex.lrequest.actionprocessor;

import static com.patex.lrequest.ResultType.Type.One;

import com.patex.lrequest.LazyActionHandler;
import com.patex.lrequest.RequestResult;
import com.patex.lrequest.ResultType;
import com.patex.lrequest.WrongActionSyntaxException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class GetFirst implements LazyActionHandler {

  @Override
  public Function execute(Supplier... params) {
    return l -> ((List) l).get(0);
  }

  @Override
  public ResultType preprocess(ResultType input, RequestResult... paramTypes) {
    if (paramTypes.length != 0) {
      throw new WrongActionSyntaxException("List.GetFirst");
    }
    return new ResultType(One, input.getReturnType());
  }

}
