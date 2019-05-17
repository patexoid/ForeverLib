package com.patex.lrequest;

import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public class StreamValue<R> extends Value<Stream<R>> {

  private final Class<R> valuesClass;

  public StreamValue(Class<R> resultClass, Supplier<Stream<R>> resultSupplier) {
    super((Class) Stream.class, resultSupplier);
    valuesClass = resultClass;
  }
}
