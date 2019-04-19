package com.patex.lrequest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ResultType {

  private final Type type;

  private final Class returnType;

  public enum Type{
    None,
    Map,
    FlatMap,
    One,
  }
}
