package com.patex.lrequest;

import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ActionResult<I, O> {

  private final Function<Stream<I>, O> resultFunc;

  private final DataType dataType;
}
